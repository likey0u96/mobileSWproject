package com.example.healdoc_mobile_5


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_receipt.*
import kotlinx.android.synthetic.main.list_book_item.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.SystemClock
import androidx.core.content.ContextCompat.getSystemService






/**
 * A simple [Fragment] subclass.
 */
class ReceiptFragment : Fragment() {

    val database : FirebaseDatabase = FirebaseDatabase.getInstance() //DB
    var adapter : ListViewAdapter =ListViewAdapter()
    var user = "홍길동"

    var calledAlready = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        if (!calledAlready)
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true) // 다른 인스턴스보다 먼저 실행되어야 한다.
            calledAlready = true;
        }

        // DB
        val myRef : DatabaseReference = database.getReference(user).child("예약")
        var now : LocalDate = LocalDate.now()
        var today = now.format(DateTimeFormatter.ofPattern("yyyy년M월d일")) //오늘날짜

        var info : DatabaseReference
        var flag : Int = 1
        Log.w("myRef","${today}")

        if(myRef.child(today).key == today){
            info = myRef.child(today)
//            Log.w("INFOCOUNt","${info}")
            flag = 1 //존재함
//            Log.w("myRef","hihihihihi")
        }
        else {
            info = myRef
            flag = 0 //존재하지 않음
        }
        info.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(flag == 1) {
                    adapter.clearItem()
                    for (snapshot in p0.children) {
                        var post : bookInfo? = snapshot.getValue(bookInfo::class.java)
//                        Log.w("COUNT","${p0.getValue()}")
                        //오늘 날짜의 예약이 있으면 쓰기
                        adapter.addItem(today, "${post?.sub}", "${post?.hour}", "${post?.tea}")

                    }
                }
                adapter.notifyDataSetChanged()//어댑터에 리스트가 바뀜을 알린다

            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment



        return inflater.inflate(R.layout.fragment_receipt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        list_book.adapter = adapter

        list_book.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val select = "10"
            //접수신청된 진료가 있으면, 어댑터 초기화 후 대기번호 발급
            adapter.clearItem()
            adapter.addItem("접수중인 진료가 있습니다.", "${txt_sub.text}", "${txt_time.text}", "${txt_tea.text}")
            list_book.adapter = adapter
            adapter.notifyDataSetChanged()//어댑터에 리스트가 바뀜을 알린다
            list_book.isEnabled = false

            //대기번호를 랜덤으로 생성! -> 그 숫자만큼 분을 기다림
            val random = Random()
            val num = random.nextInt(5) + 2 //2-6까지 랜덤으로 생성


            txt_waitnum.text = "$num"


//            val intent = Intent(context, ReceiptReceiver::class.java)
//
//            val sender = PendingIntent.getBroadcast(this, 0, intent, 0)
//
//            // 알람을 받을 시간을 num분 뒤로 설정
//            val calendar = Calendar.getInstance()
//            calendar.timeInMillis = System.currentTimeMillis()
//            calendar.add(Calendar.SECOND, num)
//
//            // 알람 매니저에 알람을 등록
//
//            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)

            var alarmMgr: AlarmManager? = null
            lateinit var alarmIntent: PendingIntent

            alarmMgr = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmIntent = Intent(context, ReceiptReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(context, 0, intent, 0)
            }
            var calendar : Calendar = Calendar.getInstance()
            calendar.setTimeInMillis(System.currentTimeMillis())
            calendar.add(Calendar.SECOND, 5)
            //5초뒤 울리는 알람
            alarmMgr?.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmIntent
            )
        }

    }
}