package internal

import MfmNode
import TEXT

fun <T : MfmNode> mergeText(nodes:List<T>):List<T>{
    val dest = mutableListOf<Any?>()
    val storedChars = mutableListOf<String>()

    fun generateText(){
        if(storedChars.size > 0){
            dest.add(TEXT(storedChars.joinToString("")))
            storedChars.clear()
        }
    }


}
