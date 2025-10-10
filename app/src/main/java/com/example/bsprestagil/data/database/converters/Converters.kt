package com.example.bsprestagil.data.database.converters

import androidx.room.TypeConverter
import com.example.bsprestagil.data.database.entities.ReferenciaEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    
    // Converters para List<String>
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    // Converters para List<ReferenciaEntity>
    @TypeConverter
    fun fromReferenciaList(value: List<ReferenciaEntity>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toReferenciaList(value: String): List<ReferenciaEntity> {
        val listType = object : TypeToken<List<ReferenciaEntity>>() {}.type
        return gson.fromJson(value, listType)
    }
}

