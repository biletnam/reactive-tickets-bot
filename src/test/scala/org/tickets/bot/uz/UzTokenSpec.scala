package org.tickets.bot.uz

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import com.google.common.base.Supplier
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.{Await, Future}

/**
  * Created by Bogdan_Snisar on 9/30/2016.
  */
class UzTokenSpec extends FlatSpec with BeforeAndAfterAll with Matchers {

  implicit var system: ActorSystem = null

  override protected def beforeAll(): Unit = {
    system = ActorSystem("sys")
  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val body =
    """
      |var em = $v.rot13(GV.site.email_support);$$v('#contactEmail').attach({ href: 'mailto:' + em, innerHTML: em});$v.domReady(function () {Common.performModule();Common.pageInformation();Common.setOpacHover($$v('#footer .cards_ribbon a, #footer .left a'), 50);Common.setOpacHover($$v('#footer .right a'), 70);});var _gaq = _gaq || [];_gaq.push(['_setAccount', 'UA-33134148-1']);_gaq.push(['_trackPageview']);$$_=~[];$$_={___:++$$_,$$$$:(![]+"")[$$_],__$:++$$_,$_$_:(![]+"")[$$_],_$_:++$$_,$_$$:({}+"")[$$_],$$_$:($$_[$$_]+"")[$$_],_$$:++$$_,$$$_:(!""+"")[$$_],$__:++$$_,$_$:++$$_,$$__:({}+"")[$$_],$$_:++$$_,$$$:++$$_,$___:++$$_,$__$:++$$_};$$_.$_=($$_.$_=$$_+"")[$$_.$_$]+($$_._$=$$_.$_[$$_.__$])+($$_.$$=($$_.$+"")[$$_.__$])+((!$$_)+"")[$$_._$$]+($$_.__=$$_.$_[$$_.$$_])+($$_.$=(!""+"")[$$_.__$])+($$_._=(!""+"")[$$_._$_])+$$_.$_[$$_.$_$]+$$_.__+$$_._$+$$_.$;$$_.$$=$$_.$+(!""+"")[$$_._$$]+$$_.__+$$_._+$$_.$+$$_.$$;$$_.$=($$_.___)[$$_.$_][$$_.$_];$$_.$($$_.$($$_.$$+"\""+(![]+"")[$$_._$_]+$$_._$+$$_.$$__+$$_.$_$_+(![]+"")[$$_._$_]+"\\"+$$_.__$+$$_._$_+$$_._$$+$$_.__+$$_._$+"\\"+$$_.__$+$$_.$$_+$$_._$_+$$_.$_$_+"\\"+$$_.__$+$$_.$__+$$_.$$$+$$_.$$$_+".\\"+$$_.__$+$$_.$$_+$$_._$$+$$_.$$$_+$$_.__+"\\"+$$_.__$+$$_.__$+$$_.__$+$$_.__+$$_.$$$_+"\\"+$$_.__$+$$_.$_$+$$_.$_$+"(\\\"\\"+$$_.__$+$$_.$__+$$_.$$$+"\\"+$$_.__$+$$_.$$_+$$_.$$_+"-"+$$_.__+$$_._$+"\\"+$$_.__$+$$_.$_$+$$_._$$+$$_.$$$_+"\\"+$$_.__$+$$_.$_$+$$_.$$_+"\\\",\\"+$$_.$__+$$_.___+"\\\""+$$_._$_+$$_.$$$+$$_.$$$+$$_.__$+$$_.$$$_+$$_.$$_$+$$_.$__+$$_.$_$_+$$_.$$_+$$_.___+$$_.$___+$$_.$_$$+$$_.$$__+$$_.$$$$+$$_.$$_+$$_.$$_+$$_.$___+$$_.$_$+$$_.$$_+$$_.$$$+$$_._$_+$$_.$$$$+$$_.$$__+$$_.$__$+$$_.$__$+$$_.$$_$+$$_.$$_+$$_.__$+$$_.$__$+$$_.__$+$$_.$$_+$$_.$_$+"\\\");"+"\"")())();(function () {var ga = document.createElement('script');ga.async = true;ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';var s = document.getElementsByTagName('script')[0];s.parentNode.insertBefore(ga, s);})();
    """.stripMargin

  "A UzToken " should " be populated by token" in {
    implicit val mt = ActorMaterializer()
    implicit val ec = system.dispatcher

    val resp = HttpResponse(entity = HttpEntity(body))

    val supplier: Supplier[String] = new Supplier[String] with UzToken {
      import scala.concurrent.duration._
      private lazy val token = Await.result(loadToken(() => Future.successful(resp)), 4.seconds)
      override def get(): String = token
    }

    val res1 = supplier.get()
    res1 shouldEqual "2771ed4a608bcf6685672fc99d619165"
  }


}
