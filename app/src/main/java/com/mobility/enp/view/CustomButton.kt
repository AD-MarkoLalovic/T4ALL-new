package com.mobility.enp.view

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.mobility.enp.R

class CustomButton : ConstraintLayout {

    // Konstruktor koji se poziva pri programskom instanciranju komponente
    constructor(context: Context) : super(context) {
        initButtonResources(null)
    }

    // Konstruktor koji se poziva prilikom instanciranja putem XML-a
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initButtonResources(attrs)
    }

    // Konstruktor koji radi s prilagođenim stilom
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initButtonResources(attrs)
    }

    // Inicijalizacija resursa prilagođenog dugmeta na temelju atributa
    private fun initButtonResources(attrs: AttributeSet?) {
        // Inflacija definisanog izgleda prilagođenog dugmeta unutar trenutnog ConstraintLayouta
        val view = LayoutInflater.from(context).inflate(R.layout.custom_button_layout, this)
        //Preuzimanje i postavljanje vrednosti atributa
        context.obtainStyledAttributes(attrs, R.styleable.CustomButton).let {

            val title = it.getString(R.styleable.CustomButton_my_custom_text).orEmpty()

            val drawable = it.getDrawable(R.styleable.CustomButton_my_drawable)

            val textColor = it.getColor(
                R.styleable.CustomButton_text_color,
                context.resources.getColor(R.color.white, null)
            )

            // Preuzimanje i postavljanje vrednosti atributa za stil teksta
            val textStyleEnum = it.getInt(R.styleable.CustomButton_text_style, Typeface.NORMAL)
            //Mapiram vrednost tipa "enum" na odgovarajuci stil teksta
            val textStyle: Int = when (textStyleEnum) {
                1 -> Typeface.BOLD
                else -> Typeface.NORMAL
            }
            // Postavljanje stila teksta prilagođenog dugmeta
            view.findViewById<TextView>(R.id.btn_text).setTypeface(null, textStyle)


            val strokeColor = it.getColor(
                R.styleable.CustomButton_stroke_color,
                context.resources.getColor(R.color.clicked_background, null)
            )

            val strokeWidth = it.getDimensionPixelSize(R.styleable.CustomButton_stroke_width, 1)

            // Oslobadjanje resursa nakon upotrebe
            it.recycle()

            // Postavljanje teksta i boje teksta prilagodjenog dugmeta
            view.findViewById<TextView>(R.id.btn_text).text = title
            view.findViewById<TextView>(R.id.btn_text).setTextColor(textColor)

            // Postavljanje pozadine prilagodjenog dugmeta na temelju drawable resursa
            view.background = drawable

            // Postavljanje boje ivica (stroke) prilagođenog dugmeta
            (view.background as? GradientDrawable)?.setStroke(strokeWidth, strokeColor)

        }


    }
}