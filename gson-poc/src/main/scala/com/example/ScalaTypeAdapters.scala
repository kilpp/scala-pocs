package com.example

import com.google.gson.{Gson, TypeAdapter, TypeAdapterFactory}
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.{JsonReader, JsonToken, JsonWriter}

import java.lang.reflect.ParameterizedType

// `None` -> JSON null, `Some(v)` -> the inner value's JSON.
final class OptionTypeAdapterFactory extends TypeAdapterFactory:
  override def create[T](gson: Gson, tt: TypeToken[T]): TypeAdapter[T] =
    if !classOf[Option[?]].isAssignableFrom(tt.getRawType) then null
    else
      val inner = tt.getType match
        case pt: ParameterizedType => pt.getActualTypeArguments()(0)
        case _                     => classOf[AnyRef]
      val innerAdapter = gson.getAdapter(TypeToken.get(inner)).asInstanceOf[TypeAdapter[AnyRef]]
      new OptionTypeAdapter(innerAdapter).asInstanceOf[TypeAdapter[T]]

private final class OptionTypeAdapter(inner: TypeAdapter[AnyRef]) extends TypeAdapter[Option[AnyRef]]:
  override def write(out: JsonWriter, value: Option[AnyRef]): Unit = value match
    case Some(v) => inner.write(out, v)
    case None    => out.nullValue()
  override def read(in: JsonReader): Option[AnyRef] =
    if in.peek() == JsonToken.NULL then { in.nextNull(); None }
    else Some(inner.read(in))

// Read/write `scala.collection.immutable.List[T]` as a JSON array.
final class ScalaListTypeAdapterFactory extends TypeAdapterFactory:
  override def create[T](gson: Gson, tt: TypeToken[T]): TypeAdapter[T] =
    if !classOf[scala.collection.immutable.List[?]].isAssignableFrom(tt.getRawType) then null
    else
      val inner = tt.getType match
        case pt: ParameterizedType => pt.getActualTypeArguments()(0)
        case _                     => classOf[AnyRef]
      val innerAdapter = gson.getAdapter(TypeToken.get(inner)).asInstanceOf[TypeAdapter[AnyRef]]
      new ScalaListTypeAdapter(innerAdapter).asInstanceOf[TypeAdapter[T]]

private final class ScalaListTypeAdapter(inner: TypeAdapter[AnyRef])
    extends TypeAdapter[List[AnyRef]]:
  override def write(out: JsonWriter, value: List[AnyRef]): Unit =
    if value == null then out.nullValue()
    else
      out.beginArray()
      value.foreach(v => inner.write(out, v))
      out.endArray()
  override def read(in: JsonReader): List[AnyRef] =
    if in.peek() == JsonToken.NULL then { in.nextNull(); null }
    else
      val buf = scala.collection.mutable.ListBuffer.empty[AnyRef]
      in.beginArray()
      while in.hasNext do buf += inner.read(in)
      in.endArray()
      buf.toList
