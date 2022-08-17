package me.mnedokushev.zio.performance.testing.runner

import me.mnedokushev.zio.performance.testing.core.Scenario
import me.mnedokushev.zio.performance.testing.core.Session.FeederSession
import me.mnedokushev.zio.performance.testing.feeder.json.default.FeederJsonDefault
//import me.mnedokushev.zio.performance.testing.http.default.DefaultHttpAction
import zio.json.DeriveJsonDecoder

class Runner {

  case class Feed(foo: String)

  implicit val feedDecoder = DeriveJsonDecoder.gen[Feed]

  val scn = Scenario("test", FeederJsonDefault("""[{"foo": "bar"}]"""))
//
//  val dhp = new DefaultHttpAction[FeederSession[Feed]]
//
//  scn.exec(s => dhp.get(s"/help/${s.feed.foo}"))

}
