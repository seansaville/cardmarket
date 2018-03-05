package uk.co.seansaville.cardmarket

class AuthenticationSpec extends UnitSpec {
  def quoted(str: String): String = "\"" + str + "\""

  // Well-known credentials taken from the API documentation!
  val appToken = "bfaD9xOU0SXBhtBP"
  val accessToken = "lBY1xptUJ7ZJSK01x4fNwzw8kAe5b10Q"
  val appSecret = "pChvrpp6AEOEwxBIIUBOvWcRG3X9xL4Y"
  val accessTokenSecret = "hc1wJAOX02pGGJK2uAv1ZOiwS7I9Tpoe"
  val testCredentials = Credentials(accessToken, accessTokenSecret, appToken, appSecret)

  // Test request data
  val url = "https://www.mkmapi.eu/ws/v2.0/output.json/account"
  val request = "GET"

  "Authentication.buildOAuthHeader" should "correctly build an OAuthHeader" in {
    val header = Authentication.buildOAuthHeader(testCredentials, url, request)

    assert(header.realm == url)
    assert(header.version == "1.0")
    assert(header.timestamp != "")
    assert(header.nonce != "")
    assert(header.consumerKey == testCredentials.appToken)
    assert(header.token == testCredentials.accessToken)
    assert(header.signatureMethod == "HMAC-SHA1")
    assert(header.signature != "")
  }

  "OAuthHeader.asString" should "produce a valid OAuth 1.0 header string" in {
    val header = Authentication.buildOAuthHeader(testCredentials, url, request).asString

    assert(header.startsWith("OAuth "))
    assert(header.contains("realm=" + quoted(url)))
    assert(header.contains("oauth_version=" + quoted("1.0")))
    assert(header.contains("oauth_timestamp="))
    assert(header.contains("oauth_nonce="))
    assert(header.contains("oauth_consumer_key=" + quoted(testCredentials.appToken)))
    assert(header.contains("oauth_token=" + quoted(testCredentials.accessToken)))
    assert(header.contains("oauth_signature_method=" + quoted("HMAC-SHA1")))
    assert(header.contains("oauth_signature="))
  }
}
