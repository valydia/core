package com.github.gvolpe.smartbackpacker.parser

import cats.effect.Sync
import com.github.gvolpe.smartbackpacker.config.SBConfiguration
import com.github.gvolpe.smartbackpacker.model.AirlineReview
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Document

object TripAdvisorAirlinesParser {
  def apply[F[_] : Sync]: TripAdvisorAirlinesParser[F] = new TripAdvisorAirlinesParser[F]()
}

class TripAdvisorAirlinesParser[F[_] : Sync] {

  def airlineReviewsFor(airlineName: String): F[AirlineReview] = Sync[F].delay {
    parseAirlineReviews(airlineName)
      .getOrElse(AirlineReview(airlineName, 0.0, "", "", ""))
  }

  // TODO: Make this request asynchronous
  private def htmlDocument(airlineName: String): Document = {
    val browser = new JsoupBrowser()
    val page = SBConfiguration.airlineReviewPage(airlineName).getOrElse("http://www.google.com")
    browser.get(page)
  }

  // TODO: Use applicative to parse in parallel
  private def parseAirlineReviews(airlineName: String): Option[AirlineReview] = {
    val doc = htmlDocument(airlineName)

    val rating = for {
      airline <- doc >> elementList(".airlineRating")
      span    <- airline >?> extractor(".prw_rup span")
      result  <- span >?> attr("content")
    } yield result

    val contactInfo = for {
      info    <- doc >> elementList("#contact_info")
      address <- (info >?> extractor(".address", text)).map(_.drop(14))
      hlink   <- info >?> element(".website a")
      website <- hlink >?> attr("href")
    } yield (address, website)

    val logo = for {
      header  <- doc >> elementList(".header_logo")
      source  <- header >?> element("img")
      image   <- source >?> attr("src")
      if image.contains("airlines/logos")
    } yield image

    for {
      r <- rating.headOption
      i <- contactInfo.headOption
      l <- logo.headOption
    } yield AirlineReview(airlineName, r.toDouble, i._1, i._2, l)
  }

}
