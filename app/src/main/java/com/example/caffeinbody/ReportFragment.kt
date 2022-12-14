package com.example.caffeinbody

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.caffeinbody.DetailActivity.Companion.getMonth
import com.example.caffeinbody.DetailActivity.Companion.getYear
import com.example.caffeinbody.databinding.FragmentReportBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.DayOfWeek
import java.text.SimpleDateFormat
import java.util.*


class ReportFragment:Fragment() {
    private val binding: FragmentReportBinding by lazy {
        FragmentReportBinding.inflate(
            layoutInflater
        )
    }
    private var weekCafArray : Array<Float> = Array(7){0f}
    private val nowTime = Calendar.getInstance().getTime()
    private val weekdayFormat = SimpleDateFormat("EE", Locale.getDefault())
    private val weekDay = weekdayFormat.format(nowTime)
    private val curTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val curTime = curTimeFormat.format(nowTime)
    private var subPoint = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setChartView(binding.root)
        initCalendarView()
        initPersonal()
        initPieChart(binding.piechart)

        setCaffeineColorsDates(getMonth(), getYear())

        return binding.root
    }

    fun resetAlarm() {
        val resetAlarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val resetIntent = Intent(context, ResetTodayCaf::class.java)
        val resetSender = PendingIntent.getBroadcast(context, 0, resetIntent, PendingIntent.FLAG_IMMUTABLE)

        // ?????? ??????
        val resetCal = Calendar.getInstance()
        resetCal.timeInMillis = System.currentTimeMillis()
        resetCal[Calendar.HOUR_OF_DAY] = 0
        resetCal[Calendar.MINUTE] = 0
        resetCal[Calendar.SECOND] = 0

        //????????? 0?????? ????????? ?????? 24????????? ????????? ????????? AlarmManager.INTERVAL_DAY??? ?????????.
        resetAlarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP, resetCal.timeInMillis
                    + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, resetSender
        )
        val format = SimpleDateFormat("MM/dd kk:mm:ss")
        val setResetTime = format.format(Date(resetCal.timeInMillis + AlarmManager.INTERVAL_DAY))
        Log.d("resetAlarm", "ResetHour : $setResetTime")
    }

    fun setWeeks(){

        Log.e("time: ??????", weekDay)
        Log.e("curTime: ??????", curTime)
    }

    fun saveWeekCafJson(date: Int, caffeine: Float, week: Int){
        val prefs = App.prefs.weekCafJson
        var tmpJsonRead = JSONArray()

        if (prefs == null){
            val tmpJsonObjectSave = JSONObject()
            for (i in 0..6){
                if (date - 1==i)
                    tmpJsonRead.put(caffeine)
                else
                    tmpJsonRead.put(0)
            }
            tmpJsonObjectSave.put(getMonth().toString(), tmpJsonRead)
        }
    }

    private fun setChartView(view: View) {
        var chartWeek = binding.barchart
        //var chartWeekLine = binding.linechart
        setWeek(chartWeek)
        //setWeekLine(chartWeekLine)
    }

    private fun initBarDataSet(barDataSet: BarDataSet) {
        //Changing the color of the bar
        barDataSet.color = Color.parseColor("#304567")
        //Setting the size of the form in the legend
        barDataSet.formSize = 15f
        //showing the value of the bar, default true if not set
        barDataSet.setDrawValues(false)
        //setting the text size of the value of the bar
        barDataSet.valueTextSize = 12f
    }

    private fun setWeek(barChart: BarChart) {
//        for (i in 0..6) {
//            weekCafArray.add(0)
//        }
        setWeeks()
        resetAlarm()
        initBarChart(barChart)

        barChart.setScaleEnabled(false) //Zoom In/Out

        val valueList = ArrayList<Float?>()
        val entries: ArrayList<BarEntry> = ArrayList()
        val title = "????????? ????????? ?????????"
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        if(App.prefs.monCaf!=0f) weekCafArray[0] = App.prefs.monCaf!!
        if(App.prefs.tueCaf!=0f) weekCafArray[1] = App.prefs.tueCaf!!
        if(App.prefs.wedCaf!=0f) weekCafArray[2] = App.prefs.wedCaf!!
        if(App.prefs.thuCaf!=0f) weekCafArray[3] = App.prefs.thuCaf!!
        if(App.prefs.friCaf!=0f) weekCafArray[4] = App.prefs.friCaf!!
        if(App.prefs.satCaf!=0f) weekCafArray[5] = App.prefs.satCaf!!
        if(App.prefs.sunCaf!=0f) weekCafArray[6] = App.prefs.sunCaf!!

        when(dayOfWeek) {
            2 -> weekCafArray[0] = App.prefs.todayCaf!!
            3 -> weekCafArray[1] = App.prefs.todayCaf!!
            4 -> weekCafArray[2] = App.prefs.todayCaf!!
            5 -> weekCafArray[3] = App.prefs.todayCaf!!
            6 -> weekCafArray[4] = App.prefs.todayCaf!!
            7 -> weekCafArray[5] = App.prefs.todayCaf!!
            1 -> weekCafArray[6] = App.prefs.todayCaf!!
        }

        for (i in 0..6) {
            //????????? ????????? ????????? ?????? ?????????
            valueList.add(weekCafArray.get(i))
            Log.e("weekCafArray", weekCafArray.toString())
            Log.e("weekCafJson", App.prefs.weekCafJson.toString())
            Log.e("dayCaffeine", App.prefs.todayCaf.toString())
        }


        //fit the data into a bar
        for (i in 1 until valueList.size + 1) {
            val barEntry = BarEntry(i.toFloat(), valueList[i - 1]!!.toFloat())//?????????????????????
            entries.add(barEntry)
        }
        val barDataSet = BarDataSet(entries, title)
        var colorList = listOf(
            Color.parseColor("#9D5812"),
            Color.parseColor("#B17941"),
            Color.parseColor("#C49B71"),


        )
        barDataSet.setColors(colorList)
        val data = BarData(barDataSet)
        barChart.data = data
        barChart.invalidate()
    }


    private fun initPieChart(pieChart: PieChart){
        // ????????? ??????
        //  ?????? ???????????? ?????? ??? ????????? ????????? ???????????? ??? ???????????? ?????????
        //  ?????? ???????????? ???????????? ??????????????? ????????? ?????????
        //  ???????????? ????????? ?????? ??? ???????????? ??????, ?????? ???????????? ????????? ????????? ??????
        //  ???????????? ???????????? binding.piecharttext visible = GONE
        //  ????????? ???????????? ????????? binding.piechart visible = GONE

        if(App.prefs.moreThanSensitivity==null) App.prefs.moreThanSensitivity="1"

        if(App.prefs.moreThanSensitivity!=null) {
            for (i in weekCafArray) { // 1??? ?????? ??????????????? ?????? ?????? ?????? 10??? ??????
                if (i >= App.prefs.dayCaffeine!!.toFloat())
                    subPoint += 10
            }
            Log.e("subPoint", subPoint.toString())

            subPoint += 2 * (App.prefs.moreThanSensitivity!!.length-1)// 1??? ????????? or ???????????? ???????????? ???????????? ?????? ?????? ?????? 2?????? ??????
            Log.e("subPoint2", subPoint.toString())

            val score = 100-subPoint

            binding.piechart.setUsePercentValues(true)
            binding.piechart.legend.isEnabled = false

            val entries = ArrayList<PieEntry>()
            entries.add(PieEntry(score.toFloat(),""))
            entries.add(PieEntry(subPoint.toFloat(),""))
            Log.e("score",score.toString())
            Log.e("entries",entries.toString())

            val colorItems = ArrayList<Int>()
            colorItems.add(Color.parseColor("#f87e76"))
            colorItems.add(Color.WHITE)

            val pieDataSet = PieDataSet(entries, "")
            pieDataSet.apply{
                colors = colorItems
                valueTextSize = 0f
            }
            pieDataSet.setDrawValues(false)

            val pieData = PieData(pieDataSet)
            binding.piechart.apply {
                data = pieData
                description.isEnabled = false
                isRotationEnabled = false
                centerText = score.toString() + "???"
                setCenterTextSize(20f)
                setCenterTextTypeface(Typeface.DEFAULT_BOLD)
                setEntryLabelColor(Color.BLACK)
                animateY(1400, Easing.EaseInOutQuad)
                animate()
            }

            if(score in 90..100) binding.textView7.text = "???????????? ??? ???????????? ?????????! ????"
            else if(score in 70..89) binding.textView7.text = "????????? ??? ??????????????????? ????"
            else if(score in 50..69) binding.textView7.text = "?????? ????????? ????????? ??????????????? ????????? ?????????.????"
            else if(score in 0..49) binding.textView7.text = "???????????? ????????? ????????? ??? ??? ?????????.????"
        }

        if (weekCafArray == { 0 }) {
            binding.piecharttext.visibility = View.GONE
            binding.piechart.visibility = View.INVISIBLE
        } else {
            binding.piecharttext.visibility = View.GONE
            binding.piechart.visibility = View.VISIBLE
        }


    }

    private fun initPersonal(){
        var age = "??????"
        var gender = "??????"
        var isPregnant = "????????????"
        // ?????? ??????,?????? ??? + ????????????(???????????? ?????? sharedpreference??? ?????? ????????? ??????
        when(App.prefs.age){
            "minor" -> age = "????????????"
            "adult" -> age = "??????"
            "senior" -> age = "??????"
        }
        when(App.prefs.gender){
            "male" -> gender = "??????"
            "female" -> gender = "??????"
        }
        when(App.prefs.isPregnant){
            true -> isPregnant = "?????? ???"
            false -> isPregnant = "???????????? ??????"
        }
        binding.textView3.text = age + " / " + gender + " / " + isPregnant
        binding.awakenumber.text = App.prefs.awakenumber.toString()
        binding.habitnumber.text = App.prefs.habitnumber.toString()
        binding.tastenumber.text = App.prefs.tastenumber.toString()
    }
    private fun initBarChart(barChart: BarChart) {
        //hiding the grey background of the chart, default false if not set
        barChart.setDrawGridBackground(false)

        barChart.legend.isEnabled = false
        //remove the bar shadow, default false if not set
        barChart.setDrawBarShadow(false)
        //remove border of the chart, default false if not set
        barChart.setDrawBorders(false)

        //remove the description label text located at the lower right corner
        val description = Description()
        description.setEnabled(false)
        barChart.setDescription(description)

        //X, Y ?????? ??????????????? ??????
        barChart.animateY(1000)
        barChart.animateX(1000)


        //?????? ?????? ???
        val xAxis: XAxis = barChart.getXAxis()
        //change the position of x-axis to the bottom
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //set the horizontal distance of the grid line
        xAxis.granularity = 1f
        xAxis.textColor = Color.DKGRAY
        //hiding the x-axis line, default true if not set
        xAxis.setDrawAxisLine(false)
        //hiding the vertical grid lines, default true if not set
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = MyXAxisFormatter()
        xAxis.textSize = 14f


        //?????? ??? hiding the left y-axis line, default true if not set
        val leftAxis: YAxis = barChart.getAxisLeft()
        leftAxis.setDrawAxisLine(false)
        leftAxis.textColor = Color.DKGRAY


        //?????? ??? hiding the right y-axis line, default true if not set
        val rightAxis: YAxis = barChart.getAxisRight()
        // rightAxis.setDrawAxisLine(false)
        // rightAxis.set
        rightAxis.isEnabled = false

        //???????????? ?????????
        val legend: Legend = barChart.getLegend()
        //setting the shape of the legend form to line, default square shape
        legend.form = Legend.LegendForm.LINE
        //setting the text size of the legend
        legend.textSize = 11f
        legend.textColor = Color.DKGRAY
        //setting the alignment of legend toward the chart
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        //setting the stacking direction of legend
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        //setting the location of legend outside the chart, default false if not set
        legend.setDrawInside(false)
    }

    inner class MyXAxisFormatter : ValueFormatter(){
        private val days = arrayOf("???","???","???","???","???","???","???")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt()-1) ?: value.toString()
        }
    }

    fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        // Decide if to intercept or not
        return true
    }


    fun initCalendarView(){
        var test = App.prefs.monthCafJson
        Log.e("report", "year "+ getYear() + "month" + getMonth() + test.toString())
         binding.calendarview.state().edit()
            //.setMinimumDate(CalendarDay.from(getYear(), getMonth(), 1))
             //.setMaximumDate(CalendarDay.from(getYear(), getMonth(), 31))
            //.setFirstDayOfWeek(DayOfWeek.of(Calendar.SUNDAY))
            .commit()

        //????????? ??????, ??????, ?????? ?????? ????????? ??????
        binding.calendarview.setHeaderTextAppearance(R.style.CalendarWidgetHeader)
        binding.calendarview.setDateTextAppearance(R.style.CalenderViewDateCustomText)
        binding.calendarview.setWeekDayTextAppearance(R.style.CalenderViewWeekCustomText)
        //binding.calendarview.setWeekDayFormatter(ArrayWeekDayFormatter(resources.getTextArray(R.array.custom_weekdays)))
        //binding.calendarview.setTitleFormatter(MonthArrayTitleFormatter(getResources().getTextArray(R.array.custom_months)))
        //binding.calendarview.setWeekDayFormatter(ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)))


        // ?????? ?????? ??? ?????? ????????? ??????????????? ??????????????? ???
        binding.calendarview.setOnRangeSelectedListener { widget, dates ->
            val startDay = dates[0].date.toString()
            val endDay = dates[dates.size - 1].date.toString()
            Log.e("Calendar", "????????? : $startDay, ????????? : $endDay")
        }
        /*binding.calendarview.setOnRangeSelectedListener({
                widget, dates ->
                val startDay = dates[0].date.toString()
                val endDay = dates[dates.size - 1].date.toString()
                Log.e("Calendar", "????????? : $startDay, ????????? : $endDay")
        })*/

        binding.calendarview.setOnMonthChangedListener { widget, date ->
            //Log.e("ReportFragment", "?????? ???????????? month: " + date.month + " year: "+ date.year)
            setCaffeineColorsDates(date.month, date.year)
        }
    }

    /* ????????? ????????? background??? ???????????? Decorator ????????? */
    private class DayDecoratorGreen(context: Context?, currentDay: CalendarDay) : DayViewDecorator {
        private val drawable: Drawable?
        private var myDay = currentDay
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return myDay == day
        }

        override fun decorate(view: DayViewFacade) {
            view.setSelectionDrawable(drawable!!)
        }

        init {
            drawable = ContextCompat.getDrawable(context!!, R.drawable.calendar_caffeine_color1)
        }
    }

    private class DayDecoratorYellow(context: Context?, currentDay: CalendarDay) : DayViewDecorator {
        private val drawable: Drawable?
        private var myDay = currentDay
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return myDay == day
        }

        override fun decorate(view: DayViewFacade) {
            view.setSelectionDrawable(drawable!!)
        }

        init {
            drawable = ContextCompat.getDrawable(context!!, R.drawable.calendar_caffeine_color2)
        }
    }

    private class DayDecoratorRed(context: Context?, currentDay: CalendarDay) : DayViewDecorator {
        private val drawable: Drawable?
        private var myDay = currentDay
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return myDay == day
        }

        override fun decorate(view: DayViewFacade) {
            view.setSelectionDrawable(drawable!!)
        }

        init {
            drawable = ContextCompat.getDrawable(context!!, R.drawable.calendar_caffeine_color3)
        }
    }

    fun setCaffeineColorsDates(month: Int, year: Int){//????????? ????????? ????????? ?????? ?????? ????????? ????????? ????????????
        if (getMonthCafJson(1, month, year) == "error"){
            return
        }
        for (i in 1.. 31){
            if (getMonthCafJson(i, month, year).toInt() == 1){
                binding.calendarview.addDecorators(DayDecoratorGreen(context, (CalendarDay.from(year, month, i))))
                Log.e("ReportFragment", "green $i")
            }else if (getMonthCafJson(i, month, year).toInt() == 2){
                binding.calendarview.addDecorators(DayDecoratorYellow(context, (CalendarDay.from(year, month, i))))
                Log.e("ReportFragment", "yellow $i")
            }else if(getMonthCafJson(i, month, year).toInt() == 3){
                binding.calendarview.addDecorators(DayDecoratorRed(context, (CalendarDay.from(year, month, i))))
                Log.e("ReportFragment", "red $i")
            }else//?????? ?????? ?????? ???
                Log.e("ReportFragment", "monthCafJson??? ?????? ?????? ?????? ?????? $i")
        }
    }


    companion object{
        fun saveMonthCafJson(date: Int, color: Int, month: Int){//?????? ????????? ???????????? ?????? ????????????
            //val caffeineColors: MutableList<Int> = mutableListOf<Int>(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
            val prefs = App.prefs.monthCafJson
            var tmpJsonRead = JSONArray()

            if (prefs == null){//?????? ?????? ??????
                val tmpJsonObjectSave = JSONObject()
                for (i in 0.. 30) {
                    if (date - 1 == i)
                        tmpJsonRead.put(color)
                    else
                        tmpJsonRead.put(0)
                }
                tmpJsonObjectSave.put(getYear().toString()+"_"+getMonth().toString(), tmpJsonRead)
                App.prefs.monthCafJson = tmpJsonObjectSave.toString()
                Log.e("ReportFragment", "monthCafJson????????????")
            }else{
                val tmpJsonObjectRead = JSONObject(prefs)
                var tmpJsonArray2 = tmpJsonObjectRead.optString(getYear().toString()+"_"+getMonth().toString())
                var tmpJsonArraySave = JSONArray()//?????????
                if (tmpJsonArray2 == ""){//????????? ?????? ??????
                    for (i in 0.. 30) {
                        if (date - 1 == i)
                            tmpJsonRead.put(color)
                        else
                            tmpJsonRead.put(0)
                    }
                    tmpJsonObjectRead.put(getYear().toString()+"_"+getMonth().toString(), tmpJsonRead)
                    App.prefs.monthCafJson = tmpJsonObjectRead.toString()
                    Log.e("ReportFragment", "????????? ??? ??????")
                }else {//?????? ??? ???????????? ??????
                    tmpJsonRead = JSONArray(tmpJsonArray2)//?????????
                    for( i in 0 .. tmpJsonRead.length() -1){
                        //caffeineColors[i] = tmpJsonArray1.optString(i).toInt()////////
                        if(date - 1 == i){//??????????????? ???????????? ??? ??????
                            tmpJsonArraySave.put(color)
                        }else
                            tmpJsonArraySave.put(tmpJsonRead.optString(i).toInt())

                    }
                    tmpJsonObjectRead.put(getYear().toString()+"_"+getMonth().toString(), tmpJsonArraySave)
                    App.prefs.monthCafJson = tmpJsonObjectRead.toString()
                    Log.e("report", "?????? ???")
                }
                Log.e("ReportFragment", tmpJsonObjectRead.toString())
            }
        }

        fun getMonthCafJson(date: Int, month: Int, year:Int): String{//?????? ????????? ?????? ?????? ????????????
            //Log.e("report", "month: $month, date: $date, year: $year")
            val prefsArray = App.prefs.monthCafJson
            if (prefsArray == null) {
                Log.e("ReportFragment", "?????? ???????????? ??????")
                return "error"
            }else{
                val tmpJsonObject = JSONObject(prefsArray)
                var tmpJsonArray1 = tmpJsonObject.optString(year.toString()+"_"+month.toString())
                if (tmpJsonArray1 == ""){//?????? ???????????? ???????????? ?????? ?????????(optString()??? ?????? ?????? ??? ""??? ????????????)
                    Log.e("ReportFragment", "???????????? ?????? ?????? ?????????")
                    return "error"//0?????? ???????????? ?????????
                }else{
                    return JSONArray(tmpJsonArray1).optString(date -1 )
                }
            }
        }

        fun calMonthCaffeineColor(todayCaf: Float): Int{
            val dayCaffeine = App.prefs.dayCaffeine?.toFloat()
            if (todayCaf > dayCaffeine!!){
                return 3 //??????(????????? ??????)
            }else if(todayCaf > dayCaffeine!!/2){
                return 2//??????(???????????? ??? ??????)
            }else{
                return 1//??????(???????????? ??? ??????)
            }
        }
    }
    //object??? ?????????


}



