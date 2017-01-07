package com.github.bsnisar.tickets.misc

import java.lang.reflect.InvocationTargetException

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import org.json4s.{DefaultReaders, Formats, MappingException, Serialization}

trait Json extends DefaultReaders {

  /**
    * Main configuration for json4s
    */
  implicit val formats = org.json4s.DefaultFormats

  /**
    * Jackson support for serialization.
    */
  implicit val serialization = org.json4s.jackson.Serialization

  /**
    * Default readers (primitives, collections)
    */
//  implicit val defaults = org.json4s.DefaultReaders


  /**
    * HTTP entity => `A`
    *
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  implicit def json4sUnmarshaller[A: Manifest](
                                                implicit serialization: Serialization,
                                                formats: Formats
                                              ): FromEntityUnmarshaller[A] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset { (data, charset) =>
        try serialization.read(data.decodeString(charset.nioCharset.name))
        catch {
          case MappingException("unknown error",
          ite: InvocationTargetException) =>
            throw ite.getCause
        }
      }

  /**
    * `A` => HTTP entity
    *
    * @tparam A type to encode, must be upper bounded by `AnyRef`
    * @return marshaller for any `A` value
    */
  implicit def json4sMarshaller[A <: AnyRef](
                                              implicit serialization: Serialization,
                                              formats: Formats
                                            ): ToEntityMarshaller[A] =
    Marshaller.StringMarshaller.wrap(`application/json`)(
      serialization.write[A]
    )
}
