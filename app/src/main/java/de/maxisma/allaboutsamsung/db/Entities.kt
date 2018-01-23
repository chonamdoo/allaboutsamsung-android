package de.maxisma.allaboutsamsung.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import java.util.Date

typealias CategoryId = Int
typealias TagId = Int
typealias PostId = Long
typealias UserId = Int

@Entity(
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["author"], onDelete = ForeignKey.RESTRICT)
    ]
)
data class Post(
    @PrimaryKey val id: PostId,
    val dateUtc: Date,
    val slug: String,
    val link: String,
    val title: String,
    val content: String,
    @ColumnInfo(index = true) val author: Int,
    val imageUrl: String?
)

@Entity(
    primaryKeys = ["postId", "categoryId"],
    foreignKeys = [
        ForeignKey(entity = Post::class, parentColumns = ["id"], childColumns = ["postId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class PostCategory(
    @ColumnInfo(index = true) val postId: PostId,
    @ColumnInfo(index = true) val categoryId: CategoryId
)

@Entity(
    primaryKeys = ["postId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = Post::class, parentColumns = ["id"], childColumns = ["postId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Tag::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class PostTag(
    @ColumnInfo(index = true) val postId: PostId,
    @ColumnInfo(index = true) val tagId: TagId
)

@Entity
data class Category(
    @PrimaryKey val id: CategoryId,
    val count: Int,
    val description: String,
    val name: String,
    val slug: String
)

@Entity
data class Tag(
    @PrimaryKey val id: TagId,
    val count: Int,
    val description: String,
    val name: String,
    val slug: String
)

@Entity(
    foreignKeys = [
        ForeignKey(entity = Tag::class, parentColumns = ["id"], childColumns = ["id"], onDelete = ForeignKey.RESTRICT)
    ]
)
data class TagSubscription(@PrimaryKey val id: TagId)

@Entity(
    foreignKeys = [
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["id"], onDelete = ForeignKey.RESTRICT)
    ]
)
data class CategorySubscription(@PrimaryKey val id: CategoryId)

@Entity
data class User(
    @PrimaryKey val id: UserId,
    val name: String
)