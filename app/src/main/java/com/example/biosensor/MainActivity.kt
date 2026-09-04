package com.example.biosensor

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private var tecnicaAtual = "VOQ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Margens do sistema
        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Navegação BottomBar
        val layoutDashboard = findViewById<ScrollView>(R.id.layoutDashboard)
        val layoutDatabase = findViewById<LinearLayout>(R.id.layoutDatabase)
        val layoutHistory = findViewById<LinearLayout>(R.id.layoutHistory)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> { layoutDashboard.visibility = View.VISIBLE; layoutDatabase.visibility = View.GONE; layoutHistory.visibility = View.GONE; true }
                R.id.nav_database -> { layoutDashboard.visibility = View.GONE; layoutDatabase.visibility = View.VISIBLE; layoutHistory.visibility = View.GONE; true }
                R.id.nav_history -> { layoutDashboard.visibility = View.GONE; layoutDatabase.visibility = View.GONE; layoutHistory.visibility = View.VISIBLE; true }
                else -> false
            }
        }

        // Elementos do Dashboard
        lineChart = findViewById(R.id.lineChart)
        val toggleGroupTecnica = findViewById<MaterialButtonToggleGroup>(R.id.toggleGroupTecnica)
        val btnProcessar = findViewById<Button>(R.id.btnProcessarGrafico)
        val txtTituloGrafico = findViewById<TextView>(R.id.txtTituloGrafico)
        val txtParametro1 = findViewById<TextView>(R.id.txtParametro1)
        val txtParametro2 = findViewById<TextView>(R.id.txtParametro2)
        val txtDiagnosticoValor = findViewById<TextView>(R.id.txtDiagnosticoValor)
        val txtDiagnosticoDetalhe = findViewById<TextView>(R.id.txtDiagnosticoDetalhe)

        val labelEixoY = findViewById<TextView>(R.id.labelEixoY)
        val labelEixoX = findViewById<TextView>(R.id.labelEixoX)

        configurarGraficoVazio()

        // Seleção de Técnica (VOQ / EIE)
        toggleGroupTecnica.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btnVOQ) {
                    tecnicaAtual = "VOQ"
                    txtTituloGrafico.text = "Curva: Corrente vs Potencial"
                    btnProcessar.text = "Executar Leitura (VOQ)"
                    txtParametro1.text = "Ip (Corrente de Pico): -- µA"
                    txtParametro2.text = "Ep (Potencial de Pico): -- V"
                    txtParametro2.visibility = View.VISIBLE

                    labelEixoY.text = "Ip (µA)"
                    labelEixoX.text = "Ep (V)"
                    labelEixoY.visibility = View.VISIBLE
                    labelEixoX.visibility = View.VISIBLE
                } else if (checkedId == R.id.btnEIE) {
                    tecnicaAtual = "EIE"
                    txtTituloGrafico.text = "Curva: Impedância (Nyquist)"
                    btnProcessar.text = "Executar Leitura (EIE)"
                    txtParametro1.text = "Rct (Resist. de Transf.): -- Ω"
                    txtParametro2.visibility = View.GONE

                    labelEixoY.text = "-Z'' (imaginária)"
                    labelEixoX.text = "Z' (real)"
                }
                configurarGraficoVazio()
            }
        }

        // Executar Leitura
        btnProcessar.setOnClickListener {
            Toast.makeText(this, "Aquisição via Firmware ($tecnicaAtual)...", Toast.LENGTH_SHORT).show()

            if (tecnicaAtual == "VOQ") {
                val ep = String.format("%.2f", Random.nextDouble(0.38, 0.48)).replace(',', '.')
                val ip = String.format("%.1f", Random.nextDouble(12.0, 22.0)).replace(',', '.')
                val nivelUrico = String.format("%.1f", Random.nextDouble(3.5, 7.8)).replace(',', '.')
                val valorNumerico = nivelUrico.toDouble()

                txtParametro1.text = "Ip (Corrente de Pico): $ip µA"
                txtParametro2.text = "Ep (Potencial de Pico): $ep V"

                val statusClinico = if (valorNumerico <= 7.0) {
                    "$nivelUrico mg/dL - Nível Estável"
                } else {
                    "$nivelUrico mg/dL - Alerta: Elevado"
                }
                val similaridade = Random.nextInt(95, 99)

                txtDiagnosticoValor.text = statusClinico
                txtDiagnosticoDetalhe.text = "Perfil eletroquímico compatível com $similaridade% de similaridade no banco local."

                atualizarGraficoVOQ(ip.toFloat(), ep.toFloat())
            } else {
                // Simulação EIE
                val rct = Random.nextInt(180, 420)
                txtParametro1.text = "Rct (Resist. de Transf.): $rct Ω"
                txtDiagnosticoValor.text = "$rct Ω - Transferência Normal"
                txtDiagnosticoDetalhe.text = "Espectroscopia de impedância avaliada com sucesso."

                configurarGraficoVazio()
            }
        }
    }

    private fun configurarGraficoVazio() {
        lineChart.clear()
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(false)
        lineChart.xAxis.isEnabled = false
        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.textColor = Color.parseColor("#047857")
        lineChart.legend.isEnabled = false
        lineChart.setNoDataText("Selecione a técnica e execute a leitura")
        lineChart.setNoDataTextColor(Color.parseColor("#047857"))
        lineChart.invalidate()
    }

    private fun atualizarGraficoVOQ(correntePico: Float, potencialPico: Float) {
        val entries = ArrayList<Entry>()
        val minV = 0.0f
        val maxV = 0.8f
        val numPontos = 50
        val step = (maxV - minV) / numPontos

        for (i in 0..numPontos) {
            val voltagemX = minV + (i * step)
            val baseCorrente = (voltagemX * 4)
            val expoente = -Math.pow(((voltagemX - potencialPico) / 0.08).toDouble(), 2.0)
            val picoAtual = correntePico * Math.exp(expoente)
            val ruido = Random.nextDouble(-0.3, 0.3)
            val correnteY = (baseCorrente + picoAtual + ruido).toFloat()

            entries.add(Entry(voltagemX, correnteY))
        }

        // Linha e preenchimento agora em tons de verde laboratório
        val dataSet = LineDataSet(entries, "Voltametria de Onda Quadrada").apply {
            color = Color.parseColor("#059669") // Verde esmeralda forte
            setDrawValues(false)
            lineWidth = 2.5f
            setDrawCircles(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#A7F3D0") // Verde claro translúcido
            fillAlpha = 80
        }

        lineChart.data = LineData(dataSet)
        lineChart.xAxis.isEnabled = true
        lineChart.xAxis.textColor = Color.parseColor("#047857")
        lineChart.axisLeft.textColor = Color.parseColor("#047857")
        lineChart.invalidate()
    }
}