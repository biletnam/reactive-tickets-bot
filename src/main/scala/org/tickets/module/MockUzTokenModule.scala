package org.tickets.module

import com.google.common.base.{Supplier, Suppliers}
import com.google.inject.{AbstractModule, TypeLiteral}
import com.google.inject.name.Names

/**
  * Created by Bogdan_Snisar on 10/3/2016.
  */
class MockUzTokenModule extends AbstractModule {
  override def configure(): Unit = {

    bind(new TypeLiteral[Supplier[String]](){})
      .annotatedWith(Names.named("UzToken"))
      .toInstance(Suppliers.ofInstance("test-token"))
  }
}
