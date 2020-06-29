package com.example.finalexam.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.finalexam.MainActivity
import com.example.finalexam.R
import com.example.finalexam.dataClass.Post
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_board_list_item.view.*
import kotlinx.android.synthetic.main.fragment_notice_board.*
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import java.text.SimpleDateFormat
import java.util.*

class noticeBoardFragment : Fragment() {

    val posts:MutableList<Post> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notice_board, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = StaggeredGridLayoutManager(2,LinearLayoutManager.VERTICAL)
        notice_rv.layoutManager = layoutManager
        notice_rv.adapter = MyAdapter()
        floatingActionButton.setOnClickListener {
            (activity as MainActivity).setFragment(writeFragment.newInstance())
        }

        //파이어베이스에서 게시글읽어오기
        FirebaseDatabase.getInstance().getReference("/Posts")
            .orderByChild("writeTime").addChildEventListener(object : ChildEventListener{
                //취소된 경우
                override fun onCancelled(snapshot: DatabaseError) {
                    snapshot?.toException()?.printStackTrace()
                }

                //글의 순서가 이동된 경우
                override fun onChildMoved(snapshot: DataSnapshot, prevChildKey: String?) {
                    snapshot?.let {
                        val post = snapshot.getValue(Post::class.java)

                        post?.let { post ->
                            //기존의 인덱스를 구한다.
                            val existIndex = posts.map { it.postId }.indexOf(post.postId)
                            //기존의 데이터를 지운다.
                            posts.removeAt(existIndex)
                            notice_rv.adapter?.notifyItemRemoved(existIndex)

                            //prevChildKey가 없는 경우 맨 마지막으로 이동 된것
                            if (prevChildKey == null){
                                posts.add(post)
                                notice_rv.adapter?.notifyItemChanged(posts.size - 1)
                            }else{
                                //prev키 다음글로 추가한다.
                                val prevIndex = posts.map { it.postId }.indexOf(prevChildKey)
                                posts.add(prevIndex + 1,post)
                                notice_rv.adapter?.notifyItemChanged(prevIndex + 1)
                            }
                        }
                    }
                }

                //글이 변경되 경우
                override fun onChildChanged(snapshot: DataSnapshot, prevChildKey: String?) {
                    snapshot?.let { snapshot ->
                        //snapshot의 데이터를 뽑아 Post객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)
                        post?.let { post ->
                            //글이 변경된 경우 글의 앞의 데이터 인덱스에 데이터를 변경한다.
                            val prevIndex = posts.map { it.postId }.indexOf(prevChildKey)
                            posts[prevIndex + 1] = post
                            notice_rv.adapter?.notifyItemChanged(prevIndex + 1)
                        }
                    }
                }

                //글이 추가된 경우
                override fun onChildAdded(snapshot: DataSnapshot, prevChildKey: String?) {
                    snapshot?.let { snapshot ->
                        //snap의 데이터를 Post객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)
                        post?.let {

                            //새글이 마지막 부분에 추가된 경우
                            if(prevChildKey == null){
                                //글 목록을 저장하는 변수에 post객체 추가
                                posts.add(it)
                                //리사이클러 뷰에 글이 추가된것을 알린다.
                                notice_rv.adapter?.notifyItemInserted(posts.size -1)
                            }else{
                                //글이 중간에 삽입된 경우 prevChildKey로 한단계 앞의 데이터의 위피를 찾은 뒤 추가한다.
                                val prevIndex = posts.map { it.postId }.indexOf(prevChildKey)
                                posts.add(prevIndex + 1,post)
                                //리사이클러뷰 업데이트
                                notice_rv.adapter?.notifyItemInserted(prevIndex + 1)
                            }
                        }
                    }
                }

                //글이 삭제된 경우
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    snapshot?.let {
                        val post = snapshot.getValue(Post::class.java)

                        post?.let { post ->
                            val existIndex = posts.map { it.postId }.indexOf(post.postId)
                            posts.removeAt(existIndex)
                            notice_rv.adapter?.notifyItemRemoved(existIndex)
                        }
                    }
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    companion object {
        fun newInstance(): noticeBoardFragment {
            return noticeBoardFragment()
        }
    }


    inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val title = itemView.title
        val content = itemView.subtitle
        val timeTextView = itemView.timeTextView
        val commentCountText = itemView.commentCountText
        val image = itemView.imageView3

    }

    inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(requireContext()).inflate(R.layout.fragment_board_list_item,parent,false))
        }

        override fun getItemCount(): Int {
            return posts.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val post = posts[position]

            //데이터 바인딩
            Picasso.get().load(Uri.parse(post.bgUri)).fit().centerCrop().into(holder.image)
            holder.title.text = post.title
            holder.content.text = post.message
            holder.timeTextView.text = getDiffTimeText(post.writeTime as Long)
            holder.commentCountText.text = "0"
        }

        fun getDiffTimeText(targetTime: Long): String {
            val curDateTime = DateTime()
            val targetDateTime = DateTime().withMillis(targetTime)
            val diffDay = Days.daysBetween(curDateTime, targetDateTime).days
            val diffHours = Hours.hoursBetween(targetDateTime, curDateTime).hours
            val diffMinutes = Minutes.minutesBetween(targetDateTime, curDateTime).minutes
            if (diffDay == 0) {
                if (diffHours == 0 && diffMinutes == 0) {
                    return "방금 전"
                }
                return if (diffHours > 0) {
                    "" + diffHours + "시간 전"
                } else "" + diffMinutes + "분 전"
            } else {
                val format = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm")
                return format.format(Date(targetTime))
            }
        }
    }

}

