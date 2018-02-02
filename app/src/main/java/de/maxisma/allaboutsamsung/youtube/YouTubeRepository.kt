package de.maxisma.allaboutsamsung.youtube

import android.arch.lifecycle.LiveData
import com.google.api.services.youtube.YouTube
import de.maxisma.allaboutsamsung.db.Db
import de.maxisma.allaboutsamsung.db.PlaylistId
import de.maxisma.allaboutsamsung.db.SeenVideo
import de.maxisma.allaboutsamsung.db.Video
import de.maxisma.allaboutsamsung.db.VideoId
import de.maxisma.allaboutsamsung.db.importPlaylistResult
import de.maxisma.allaboutsamsung.utils.DbWriteDispatcher
import de.maxisma.allaboutsamsung.utils.IOPool
import de.maxisma.allaboutsamsung.utils.retry
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import java.io.IOException

typealias UnseenVideos = List<VideoId>

class YouTubeRepository(
    private val db: Db,
    private val youTube: YouTube,
    private val apiKey: String,
    private val playlistId: PlaylistId,
    private val onError: (IOException) -> Unit
) {

    val videos: LiveData<List<Video>> = db.videoDao.videosInPlaylist(playlistId)
    private val mutex = Mutex()

    /**
     * pageTokens where pageTokens[0] is the token for page 1, since page 0 does not have one.
     */
    private val pageTokens = mutableListOf<String>()

    init {
        launch(DbWriteDispatcher) {
            mutex.withLock {
                db.videoDao.deleteExpired()
            }
        }
    }

    fun markAsSeen(unseenVideos: UnseenVideos) = launch(DbWriteDispatcher) {
        mutex.withLock {
            db.videoDao.insertSeenVideos(unseenVideos.map { SeenVideo(it) })
        }
    }

    fun requestNewerVideos(): Deferred<UnseenVideos> = async(IOPool) {
        mutex.withLock {
            try {
                retry(IOException::class) {
                    val playlistResultDto = youTube.downloadPlaylist(apiKey, playlistId).await()
                    if (pageTokens.isNotEmpty()) {
                        pageTokens[0] = playlistResultDto.nextPageToken
                    } else {
                        pageTokens += playlistResultDto.nextPageToken
                    }
                    launch(DbWriteDispatcher) {
                        db.importPlaylistResult(playlistResultDto).join()
                    }.join()

                    val seenVideos = db.videoDao.seenVideos().map { it.id }.toHashSet()
                    playlistResultDto.playlist.map { it.videoId } - seenVideos
                }
            } catch (e: IOException) {
                e.printStackTrace()
                onError(e)
                emptyList<VideoId>()
            }
        }
    }

    fun requestOlderVideos(): Job = launch(IOPool) {
        mutex.withLock {
            try {
                retry(IOException::class) {
                    val oldestDateDb = db.videoDao.oldestDateInPlaylist(playlistId).time
                    val allResults = mutableListOf<PlaylistResultDto>()
                    while (true) {
                        val results = youTube.downloadPlaylist(apiKey, playlistId, pageTokens.lastOrNull()).await()
                        allResults += results
                        pageTokens += results.nextPageToken
                        if (results.playlist.any { it.utcEpochMs < oldestDateDb }) {
                            // We finally fetched a video older than the currently oldest one
                            break
                        }
                    }
                    launch(DbWriteDispatcher) {
                        allResults.forEach { db.importPlaylistResult(it) }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                onError(e)
            }
        }
    }

}