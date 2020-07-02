package com.example.finalexam.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalexam.MainActivity
import com.example.finalexam.R
import com.example.finalexam.dataClass.Comment
import com.example.finalexam.dataClass.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.snapshot.ChildKey
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_comment.view.*
import kotlinx.android.synthetic.main.fragment_detail.*
import java.lang.IllegalArgumentException

class detailFragment(data: Post?) : Fragment() {

    val commentList = mutableListOf<Comment>()
    var commentlistener : ChildEventListener? = null
    var commentCount = 0

    var post = data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        backButton.setOnClickListener {
            (activity as MainActivity).setFragment(noticeBoardFragment.newInstance())
        }

        //이미 있던 댓글의 수
        commentCount = post?.commentCount!!

        detail_send_comment.setOnClickListener {
            var comment = Comment()
            var newRef = FirebaseDatabase.getInstance().getReference("Comments/${post?.postId}").push()
            comment.message = commentEditText.text.toString()
            commentEditText.setText("")
            comment.writeTime = ServerValue.TIMESTAMP
//            comment.writerId = FirebaseAuth.getInstance().currentUser?.displayName.toString()
            comment.postId = post?.postId.toString()
            comment.commentId = FirebaseAuth.getInstance().currentUser?.displayName.toString()

            newRef.setValue(comment)
            Toast.makeText(requireContext(),"저장 성공했습니다.",Toast.LENGTH_LONG).show()

            //댓글수 업데이트
            var commentListCount = FirebaseDatabase.getInstance().getReference("/Posts")
                .child("${post?.postId}")
            commentListCount.child("commentCount").setValue(commentCount + 1)
            commentCount += 1

        }

        detail_title_text.text = post?.title
        detail_content_text.text = post?.message
        detail_witter_text.text = post?.writerId
        try {
            Picasso.get()
                .load(post?.bgUri)
                .fit()
                .into(detail_ImageView)
        }catch (e:IllegalArgumentException){
            e.printStackTrace()
        }

        val layoutManager = LinearLayoutManager(requireContext())
        comment_rv.layoutManager = layoutManager
        comment_rv.adapter = MyAdapter()

        //게시글의 ID로 댓글 목록에 ChildEventListener을 등록한다.
        commentlistener = FirebaseDatabase.getInstance().getReference("/Comments/${post?.postId}")
            .addChildEventListener(object : ChildEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    p0.toException()?.printStackTrace()
                }

                override fun onChildMoved(snapshot: DataSnapshot, prevChildKey: String?) {
                    if (snapshot != null){
                        val comment = snapshot.getValue(Comment::class.java)
                        comment?.let {
                            val existIndex = commentList.map { it.commentId }.indexOf(it.commentId)
                            //기존의 데이터를 지운다.
                            commentList.removeAt(existIndex)

                            //prevKey다음에 글에 추가를 한다.
                            val prevIndex = commentList.map { it.commentId }.indexOf(prevChildKey)
                            commentList.add(prevIndex + 1 , it)
                            comment_rv.adapter?.notifyItemInserted(prevIndex + 1)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, prevChildKey: String?) {
                    snapshot?.let {snapshot ->
                        val comment = snapshot.getValue(Comment::class.java)
                        comment?.let {

                            //prevKey다음에 글에 추가를 한다.
                            val prevIndex = commentList.map { it.commentId }.indexOf(prevChildKey)
                            commentList[prevIndex+1] = comment
                            comment_rv.adapter?.notifyItemChanged(prevIndex + 1)
                        }
                    }
                }

                override fun onChildAdded(snapshot: DataSnapshot, prevChildKey: String?) {
                    snapshot?.let {snapshot ->
                        val comment = snapshot.getValue(Comment::class.java)
                        comment?.let {
                            Log.d("덧글 등록","덧글 등록")
                            //새글의 마지막 부분에 추가한다.
                            val prevIndex = commentList.map { it.commentId }.indexOf(prevChildKey)
                            commentList.add(prevIndex+1,comment)
                            comment_rv.adapter?.notifyItemInserted(prevIndex + 1)
                            comment_rv.scrollToPosition(prevIndex + 1)
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    snapshot?.let {snapshot ->
                        val comment = snapshot.getValue(Comment::class.java)
                        comment?.let {

                            //새글의 마지막 부분에 추가한다.
                            val existIndex = commentList.map { it.commentId }.indexOf(comment.commentId)
                            commentList.removeAt(existIndex)
                            comment_rv.adapter?.notifyItemRemoved(existIndex)
                        }
                    }
                }
            })

    }

    override fun onPause() {
        super.onPause()
        commentlistener?.let { it1 ->
            FirebaseDatabase.getInstance().getReference("/Comments/${post?.postId}").removeEventListener(
                it1
            )
            Log.d("리스너 제거","리스너 제거")
        }

    }

    inner class MyViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
        val witter = itemView.comment_NickName_text
        val content = itemView.comment_content_text
    }

    inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(requireContext()).inflate(R.layout.card_comment,parent,false))
        }

        override fun getItemCount(): Int {
            return commentList.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val comment = commentList[position]
            comment?.let {
                holder.witter.text = comment.commentId
                holder.content.text = comment.message
            }
        }

    }

    companion object {
        fun newInstance(data : Post?): detailFragment {
            return detailFragment(data)
        }
    }
}