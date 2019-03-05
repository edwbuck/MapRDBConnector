package com.github.anicolaspp.spark.sql

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.ojai.types.{ODate, OTime, OTimestamp}
import org.ojai.{Document, Value}

object ParsableDocument {

  import collection.JavaConverters._

  implicit class ParsableDocument(document: Document) {
    def get(field: StructField) = getField(document, field)

    private def getField(doc: Document, field: StructField): Any = {
      val value = doc.getValue(field.name)

      value.getType match {
        case Value.Type.ARRAY => value.getList.toArray //TODO: handle array of maps/OJAI types
        case Value.Type.BINARY => value.getBinary
        case Value.Type.BOOLEAN => value.getBoolean
        case Value.Type.BYTE => value.getByte
        case Value.Type.DATE => value.getDate.toDate
        case Value.Type.DECIMAL => value.getDecimal
        case Value.Type.DOUBLE => value.getDouble
        case Value.Type.FLOAT => value.getFloat
        case Value.Type.INT => value.getInt
        case Value.Type.INTERVAL => null //TODO: Find the actual type that corresponds to this
        case Value.Type.LONG => value.getLong
        case Value.Type.MAP => createMap(value.getMap)
        case Value.Type.NULL => null
        case Value.Type.SHORT => value.getShort
        case Value.Type.STRING => value.getString
        case Value.Type.TIME => new java.sql.Timestamp(value.getTime.getMilliSecond)
        case Value.Type.TIMESTAMP => new java.sql.Timestamp(value.getTimestamp.getMilliSecond)
      }
    }

    private def getValue(value: Any): Any = {
      value match {
        case v: OTimestamp => new java.sql.Timestamp(v.getMilliSecond)
        case v: OTime => new java.sql.Timestamp(v.getMilliSecond)
        case v: ODate => v.toDate
        case v: java.util.Map[String, Object] => createMap(v)
        case v: Any => v
      }
    }

    private def createMap(map: java.util.Map[String, Object]): Row = {
      val scalaMap = map.asScala

      val values = scalaMap
        .keySet
        .foldLeft(List.empty[Any])((xs, field) => getValue(scalaMap(field)) :: xs)
        .reverse

      Row.fromSeq(values)
    }
  }


}
