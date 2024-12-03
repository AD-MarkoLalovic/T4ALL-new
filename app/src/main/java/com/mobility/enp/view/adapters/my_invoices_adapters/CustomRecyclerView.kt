package com.mobility.enp.view.adapters.my_invoices_adapters

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class CustomRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    // Kreiranje instance GestureDetector za detekciju gestova
    private val gestureDetector: GestureDetector

    init {
        // Kreiranje instance GestureDetector-a
        gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                // Preklapanje metode onSingleTapUp() za detekciju jednog dodira
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    // Poziv performClick() kada se detektuje jedan dodir
                    performClick()
                    // Vraćanje vrednosti true kako bi se indikator gesta završio
                    return true
                }
            })
    }

    // Preklapanje metode performClick() za rukovanje događajem klika
    override fun performClick(): Boolean {
        // Pozivanje super metode performClick() da bi se osiguralo da se klik obradi
        super.performClick()
        // Vraćanje vrednosti true kako bi se indikator događaja klika završio
        return true
    }

    // Preklapanje metode onTouchEvent() za rukovanje događajima dodira
    override fun onTouchEvent(e: MotionEvent): Boolean {
        // Prikazivanje događaja dodira GestureDetector-u i provera da li je detektiran jedan dodir
        // Ako jeste, poziva se performClick() kako bi se osiguralo rukovanje događajem klika
        // Ako nije detektiran jedan dodir, delegira se super klasi za obradu događaja dodira
        return gestureDetector.onTouchEvent(e) || super.onTouchEvent(e)
    }
}
