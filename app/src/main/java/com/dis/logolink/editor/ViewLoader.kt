package com.dis.logolink.editor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Insets
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowInsets
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.res.ResourcesCompat
import com.dis.logolink.editor.models.Component

import com.dis.logolink.gui.R
import com.dis.logolink.editor.models.Layer
import com.dis.logolink.editor.models.Level
import com.dis.logolink.gui.CanvasLoader
import kotlinx.android.synthetic.main.activity_level.*

class ViewLoader(val activity: Activity,val context: Context) {
    lateinit var level: Level
    lateinit private var btnViewIds : List<Int>
    lateinit private var cmpViewIdsList : List<List<Int>>
    lateinit private var glViewIds : List<Int>
    var inputBtnViewList =  mutableListOf<ImageButton>()
    var layerViewList= mutableListOf<MutableList<ImageView>>()
    var guidelineList = mutableListOf<Guideline>()
    val constraintSet = ConstraintSet()
    //Dictionary for drawing positions
    val gateInputs = mutableMapOf<Int, MutableList<Component>>()
    val gateInputConnections = mutableListOf<Pair<Int, Int>>()
    //TODO: gateInputConnections = guideLineTEST IF EVERYTHING WORKS PROPERLY
    val guideLineTEST = mutableListOf<Pair< Int, Pair<Int, Int>>>()

    fun mapLevelToView(){
        generateIdListsFromLevel()
        createInputView()
        createLayerViewList()
        createGuidelines()
        setInputConstraints()
        setComponentConstraints()
        mapGateInputs()
        //drawLines()
    }

    //Maps dictionary
    //Dictionary example after function:
    //[key, value]
    //[AND.view.id, OR.view.id], [AND.view.id, AND.view.id]
    //Meaning (pair #1): OR Gate Output -> AND Gate Input
    private fun mapGateInputs() {
        var key: Int
        var value: Int? = null
        var layerIndex: Int?= null
        var listIndex = 0
        //For guidelines
        var gIndex = 0

        gateInputs.forEach()
        {
            key = it.key
            //For each input list entry...
            it.value.forEach()
            {
                val componentTemp = it
                //Input gate
                if(it.toString().contains("Input", true)){
                    value = inputBtnViewList[
                            level.defaultInputList.indexOf(it)].id
                }
                //Logic gate
                else{
                    //Search for gate layer and set value to gate id
                    level.layerList.forEach() {
                        layerIndex = level.layerList.indexOf(it)
                       it.componentList.forEach() {
                            if (it == componentTemp) {
                                value =
                                    layerViewList[layerIndex!!][level.layerList[layerIndex!!].componentList.indexOf(
                                        it
                                    )].id
                            }
                        }
                    }
                    //Corresponding layer found? If so, then and set value to gate id
                    if(layerIndex != null){
                        level.layerList[layerIndex!!].componentList.forEach(){
                            if(it == componentTemp){
                                value = layerViewList[layerIndex!!][level.layerList[layerIndex!!].componentList.indexOf(it)].id
                            }
                        }
                    }
                }
                //Add to dictionary if found
                if(value != null){
                    gateInputConnections.add(listIndex, Pair(key, value!!))

                    //TODO: verify
                    var guidelineID = getLayerGuideline(key!!)
                    guideLineTEST.add(
                        gIndex,
                        Pair(
                            guidelineID,
                            Pair(value!!, key)
                        )
                    )
                    gIndex++

                    listIndex++
                }
                //Reset value
                value = null
            }
            //Reset layer index
            layerIndex = null
        }
    }

    //Get the corresponding layer guideline
    //Returns: Guideline id
    fun getLayerGuideline(componentLeftOfGuidelineID: Int): Int{
        var guidelineLayerIndex = 0
        var layerCount = 0
        layerViewList.forEach() {
            it.forEach(){
                if(it.id == componentLeftOfGuidelineID){
                    guidelineLayerIndex = layerCount
                }
            }
            layerCount++
        }
        return guidelineList[guidelineLayerIndex].id
    }

    //Draws each line from gate to gate
    fun drawLines() {
        val canvasLoader = CanvasLoader(context.resources.displayMetrics.widthPixels, context.resources.displayMetrics.heightPixels)
        val bitmap = canvasLoader.calculateLinePositions(gateInputConnections, activity)
        //val bitmap = canvasLoader.calculateLinePositionsTEST(guideLineTEST, activity)
        val background = ImageView(context)
        background.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        val params = background.layoutParams
        val backgrndID = View.generateViewId()
        background.id = backgrndID
        background.background = BitmapDrawable(activity.resources, bitmap)
        activity.addContentView(background, params)
    }

    //Creates guidelines for each layer
    private fun createGuidelines() {
        var myfloat = 0.0F
        for(layer in level.layerList) {
            val layerIndex = level.layerList.indexOf(layer)
            val guideline = Guideline(context)
            //Set constraint parameters
            val constraintParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            constraintParams.orientation = ConstraintLayout.LayoutParams.VERTICAL
            guideline.layoutParams = constraintParams

            //Create guideline
            guideline.id = glViewIds[layerIndex]
            val index = level.layerList.indexOf(layer)
            val layerSize = level.layerList.size
            myfloat = (1 / (layerSize.toFloat() + 1))
            guideline.setGuidelineBegin(((index+1)*myfloat*getScreenWidth()).toInt())

            guidelineList.add(guideline)
            activity.LevelLayout.addView(guideline)
        }
    }
    //Left
    //constraintSet.connect(currentObject.id,
    // ConstraintSet.LEFT, leftObject.id, 0)

    //Creates gate element
    private fun createLayerViewList() {
        level.layerList.forEachIndexed { layerIndex, layer ->
            val compViewList = mutableListOf<ImageView>()
            layer.componentList.forEachIndexed() { componentIndex, component ->
                val componentViewIndex = cmpViewIdsList[layerIndex][componentIndex]
                compViewList.add(createComponentView(component, componentViewIndex))
                //Fill dictionary for mapping
                gateInputs.put(componentViewIndex, component.inputList)
            }
            layerViewList.add(compViewList)
        }
    }

    //Creates input buttons
    private fun createInputView() {
        for (component in level.defaultInputList) {
            val input = ImageButton(context)
            input.layoutParams = LinearLayout.LayoutParams(
                100,
                100
            )
            //Image
            if (component.setResult())
                input.setImageResource(R.drawable.lamp_on)
            else
                input.setImageResource(R.drawable.lamp_off)
            input.setOnClickListener { imageView ->
                val btn = imageView
                if (component.setResult()) {
                    input.setImageResource(R.drawable.lamp_off)
                    !level.defaultInputList[level.defaultInputList.indexOf(component)]
                } else {
                    input.setImageResource(R.drawable.lamp_on)
                    !level.defaultInputList[level.defaultInputList.indexOf(component)]
                }
                //TODO besser nen ChangeListener für die ImageViews als en onClick Event bei den Buttons
                    //Change ImageView Ressource
                layerViewList.forEachIndexed{layerIndex, layer ->
                    layer.forEachIndexed(){imageViewIndex, imageView ->
                        if(level.layerList[layerIndex].componentList[imageViewIndex].setResult())
                            imageView.setImageResource(R.drawable.gate_true)
                        else
                            imageView.setImageResource(R.drawable.gate_false)
                    }
                }
            }
            //get corresponding id
            input.id = btnViewIds[level.defaultInputList.indexOf(component)]
            inputBtnViewList.add(input)
            activity.LevelLayout.addView(input)
        }
    }

    //Generates ids for each and every component present on the screen
    private fun generateIdListsFromLevel(){
        //Button ids
        var btnIds = mutableListOf<Int>()
        level.defaultInputList.forEach(){
            btnIds.add(View.generateViewId())
        }
        btnViewIds = btnIds

        //Component ids
        var compIdsList = mutableListOf<MutableList<Int>>()
        for (input in level.layerList){
            compIdsList.add(generateCompIds(input))
        }
        cmpViewIdsList=compIdsList

        //Guideline ids
        var glIds = mutableListOf<Int>()
        for(layer in level.layerList)
            glIds.add(View.generateViewId())
        glViewIds=glIds
    }

    //Generate ids for each component
    private fun generateCompIds(layer: Layer): MutableList<Int> {
        var compIds = mutableListOf<Int>()
        for (component in layer.componentList) {
            compIds.add(View.generateViewId())
        }
        return compIds
    }


    //Create component image
    private fun createComponentView(component: Component,id: Int) : ImageView {
        //get image
        val componentView = ImageView(context)
        val className = component::class.java.simpleName
        var imageRessource : Any
        if(component.setResult())
            imageRessource = R.drawable.gate_true
        else
            imageRessource = R.drawable.gate_false
        componentView.setImageResource(imageRessource)
        componentView.contentDescription = className.substringBefore("Gate")

        //determine size
        var heightMod = 0
        level.layerList.forEach(){
            if(it.componentList.contains(component))
                heightMod = it.componentList.size
        }
        // === Too big! -> AND Gate H:360, W:570 ===
        val height = getScreenHeight() / heightMod
        val width = getScreenWidth()/(level.layerList.size+1)
        // ===

        componentView.layoutParams = LinearLayout.LayoutParams(250,175)
        componentView.id = id
        activity.LevelLayout.addView(componentView)
        return componentView
    }

    //Set constraint for input lamps
    fun setInputConstraints(){
        val constId = R.id.LevelLayout

        constraintSet.clone(activity.LevelLayout)
        inputBtnViewList.forEach(){
            var index = inputBtnViewList.indexOf(it)
            constraintSet.connect(it.id,ConstraintSet.RIGHT,guidelineList[0].id,ConstraintSet.LEFT)
            constraintSet.connect(it.id,ConstraintSet.LEFT,constId,ConstraintSet.LEFT)
            //Top/Bottom constraints
            //Top Input
            if(index == 0){
                constraintSet.connect(it.id,ConstraintSet.TOP,constId,ConstraintSet.TOP)
                constraintSet.connect(it.id,ConstraintSet.BOTTOM,inputBtnViewList[index+1].id,ConstraintSet.TOP)
            }
            //Middle Input
            else if(index < inputBtnViewList.size-1) {
                constraintSet.connect(it.id,ConstraintSet.TOP,inputBtnViewList[index - 1].id,ConstraintSet.BOTTOM)
                constraintSet.connect(it.id,ConstraintSet.BOTTOM,inputBtnViewList[index + 1].id,ConstraintSet.TOP)
            }
            //Bottom Input
            else if(index == inputBtnViewList.size-1){
                constraintSet.connect(it.id,ConstraintSet.TOP,inputBtnViewList[index -1].id,ConstraintSet.BOTTOM)
                constraintSet.connect(it.id,ConstraintSet.BOTTOM,constId,ConstraintSet.BOTTOM)
            }
        }
    }

    //Get screen width
    @SuppressLint("ObsoleteSdkInt")
    private fun getScreenWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    //Get screen height
    @SuppressLint("ObsoleteSdkInt")
    private fun getScreenHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    //Set constraints for every component inside each layer
    private fun setComponentConstraints() {
        for (layer in layerViewList) {
            val layerIndex = layerViewList.indexOf(layer)
            for (view in layer) {
                val viewIndex = layer.indexOf(view)
                //End Gate
                if(layerIndex == layerViewList.lastIndex){
                    constraintSet.connect(view.id,ConstraintSet.LEFT,guidelineList[layerIndex].id,ConstraintSet.RIGHT)
                    constraintSet.connect(view.id,ConstraintSet.RIGHT,activity.LevelLayout.id,ConstraintSet.RIGHT)
                    constraintSet.connect(view.id,ConstraintSet.TOP,activity.LevelLayout.id,ConstraintSet.TOP)
                    constraintSet.connect(view.id,ConstraintSet.BOTTOM,activity.LevelLayout.id,ConstraintSet.BOTTOM)
                }
                //Top / First Gate
                else if(viewIndex == 0){
                    constraintSet.connect(view.id,ConstraintSet.LEFT,guidelineList[layerIndex].id,ConstraintSet.RIGHT)
                    constraintSet.connect(view.id,ConstraintSet.RIGHT,guidelineList[layerIndex+1].id,ConstraintSet.LEFT)
                    constraintSet.connect(view.id,ConstraintSet.TOP,activity.LevelLayout.id, ConstraintSet.TOP)
                    constraintSet.connect(view.id,ConstraintSet.BOTTOM,layer[viewIndex+1].id,ConstraintSet.TOP)
                }
                //Middle Gate
                else if(viewIndex != layer.lastIndex){
                    constraintSet.connect(view.id,ConstraintSet.LEFT,guidelineList[layerIndex].id,ConstraintSet.RIGHT)
                    constraintSet.connect(view.id,ConstraintSet.RIGHT,guidelineList[layerIndex+1].id,ConstraintSet.LEFT)
                    constraintSet.connect(view.id,ConstraintSet.TOP,layer[viewIndex-1].id,ConstraintSet.BOTTOM)
                    constraintSet.connect(view.id,ConstraintSet.BOTTOM,layer[viewIndex+1].id,ConstraintSet.TOP)
                }
                //Bottom / Last Gate
                else if(viewIndex == layer.lastIndex){
                    constraintSet.connect(view.id,ConstraintSet.LEFT,guidelineList[layerIndex].id,ConstraintSet.RIGHT)
                    constraintSet.connect(view.id,ConstraintSet.RIGHT,guidelineList[layerIndex+1].id,ConstraintSet.LEFT)
                    constraintSet.connect(view.id,ConstraintSet.TOP,layer[viewIndex-1].id,ConstraintSet.BOTTOM)
                    constraintSet.connect(view.id,ConstraintSet.BOTTOM,activity.LevelLayout.id,ConstraintSet.BOTTOM)
                }
                //Add and constraint gate text
                constraintText(view, activity)
            }
            activity.LevelLayout.setConstraintSet(constraintSet)
        }
    }

    //Adds and constraints text
    private fun constraintText(view: View, activity: Activity){
        val textView = TextView(context)
        textView.id = view.id * (-1)
        textView.text = view.contentDescription
        textView.gravity = Gravity.CENTER
        textView.setTextColor(Color.WHITE)
        textView.typeface = ResourcesCompat.getFont(context, R.font.font_arcadeclassic)
        val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        //Picture offset
        layoutParams.setMargins(50, 50, 0, 0)
        textView.layoutParams = layoutParams

        activity.LevelLayout.addView(textView)

        constraintSet.connect(textView.id, ConstraintSet.TOP, view.id, ConstraintSet.TOP)
        constraintSet.connect(textView.id, ConstraintSet.LEFT, view.id, ConstraintSet.LEFT)
        constraintSet.connect(textView.id, ConstraintSet.RIGHT, view.id, ConstraintSet.RIGHT)
        constraintSet.connect(textView.id, ConstraintSet.BOTTOM, view.id, ConstraintSet.BOTTOM)
    }
}