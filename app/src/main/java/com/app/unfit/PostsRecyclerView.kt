package com.app.unfit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.unfit.model.Post

class PostsRecyclerView(private val posts: List<Post>?) :
    RecyclerView.Adapter<PostsRecyclerView.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImageView: ImageView = itemView.findViewById(R.id.post_image)
        private val postDescriptionView: TextView = itemView.findViewById(R.id.post_description)
        private val postUserView: TextView = itemView.findViewById(R.id.post_user)
        private val likeCheckBox: CheckBox = itemView.findViewById(R.id.post_like)
        private val saveCheckBox: CheckBox = itemView.findViewById(R.id.post_save)
        private var post: Post? = null

        init {
            likeCheckBox.setOnClickListener { view ->
                (view as? CheckBox)?.let { checkbox ->
                    post?.isLiked = checkbox.isChecked
                }
            }

            saveCheckBox.setOnClickListener { view ->
                (view as? CheckBox)?.let { checkbox ->
                    post?.isSaved = checkbox.isChecked
                }
            }
        }

        fun bind(post: Post?) {
            this.post = post
            post?.let { safePost ->
                postUserView.text = safePost.userName
                postDescriptionView.text = safePost.description
                likeCheckBox.isChecked = safePost.isLiked
                saveCheckBox.isChecked = safePost.isSaved

                // Simply set placeholder for now
                postImageView.setImageResource(R.drawable.ic_image_placeholder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int = posts?.size ?: 0

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts?.get(position))
    }
}