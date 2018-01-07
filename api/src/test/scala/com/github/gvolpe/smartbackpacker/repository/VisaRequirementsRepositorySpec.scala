/*
 * Copyright 2017 Smart Backpacker App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.gvolpe.smartbackpacker.repository

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.common.IOAssertion
import com.github.gvolpe.smartbackpacker.common.sql.TestDBManager
import com.github.gvolpe.smartbackpacker.model._
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.{BeforeAndAfterAll, FunSuite}

class VisaRequirementsRepositorySpec extends FunSuite with IOChecker with BeforeAndAfterAll {

  override val transactor: Transactor[IO] = TestDBManager.xa.unsafeRunSync()

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDBManager.createTables.unsafeRunSync()
  }

  private lazy val repo = new PostgresVisaRequirementsRepository[IO](transactor)

  test("NOT find visa requirements") {
    IOAssertion {
      for {
        vr <- repo.findVisaRequirements("AR".as[CountryCode], "PL".as[CountryCode])
      } yield {
        assert(vr.isEmpty)
      }
    }
  }

  test("find country 'from' query") {
    check(VisaRequirementsStatement.from("AR".as[CountryCode]))
  }

  test("find country 'to' query") {
    check(VisaRequirementsStatement.to("AR".as[CountryCode]))
  }

  test("find visa requirements query") {
    check(VisaRequirementsStatement.visaRequirements(1, 2))
  }

}