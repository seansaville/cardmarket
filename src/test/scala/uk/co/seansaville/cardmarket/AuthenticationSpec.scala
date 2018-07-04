package uk.co.seansaville.cardmarket

class AuthenticationSpec extends UnitSpec {
  // Well-known credentials taken from the API documentation!
  private val appToken = "bfaD9xOU0SXBhtBP"
  private val accessToken = "lBY1xptUJ7ZJSK01x4fNwzw8kAe5b10Q"
  private val appSecret = "pChvrpp6AEOEwxBIIUBOvWcRG3X9xL4Y"
  private val accessTokenSecret = "hc1wJAOX02pGGJK2uAv1ZOiwS7I9Tpoe"
  private val testCredentials = Credentials(accessToken, accessTokenSecret, appToken, appSecret)

  // Well-known values for the other parts of the OAuth header - also taken from the documentation
  private val realm = "https://www.mkmapi.eu/ws/v1.1/account"
  private val request = "GET"
  private val version = "1.0"
  private val nonce = "53eb1f44909d6"
  private val timestamp = "1407917892"
  private val signatureMethod = "HMAC-SHA1"
  private val signature = "DLGHHYV9OsbB/ARf73psEYaNWkI="

  // Inject a fixed nonce and timestamp so that the OAuth header signature is deterministic
  private val injectNonce: Int => String = _ => nonce
  private val injectTimestamp: () => String = () => timestamp

  "Authentication.buildOAuthHeader" should "correctly build an OAuthHeader" in {
    val header = Authentication.buildOAuthHeader(
      testCredentials, realm, request, injectNonce, injectTimestamp)

    assert(header.realm == realm)
    assert(header.version == version)
    assert(header.timestamp == timestamp)
    assert(header.nonce == nonce)
    assert(header.consumerKey == testCredentials.appToken)
    assert(header.token == testCredentials.accessToken)
    assert(header.signatureMethod == signatureMethod)
    assert(header.signature == signature)
  }

  "OAuthHeader.asString" should "produce a valid OAuth 1.0 header string" in {
    val header = Authentication.buildOAuthHeader(
      testCredentials, realm, request, injectNonce, injectTimestamp).asString

    assert(header.startsWith("OAuth "))
    assert(header.contains(OAuthHeader.headerPair("realm", realm)))
    assert(header.contains(OAuthHeader.headerPair("oauth_version", version)))
    assert(header.contains(OAuthHeader.headerPair("oauth_timestamp", timestamp)))
    assert(header.contains(OAuthHeader.headerPair("oauth_nonce", nonce)))
    assert(header.contains(OAuthHeader.headerPair("oauth_consumer_key", testCredentials.appToken)))
    assert(header.contains(OAuthHeader.headerPair("oauth_token", testCredentials.accessToken)))
    assert(header.contains(OAuthHeader.headerPair("oauth_signature_method", signatureMethod)))
    assert(header.contains(OAuthHeader.headerPair("oauth_signature", signature, last = true)))
  }

  "OAuthHeader.headerPair" should "return key-value pairs in the correct format" in {
    val key = "this_is_my_key"
    val value = "this is the value"
    assert(OAuthHeader.headerPair(key, value) == "this_is_my_key=\"this is the value\", ")
    assert(OAuthHeader.headerPair(key, value, last = true) == "this_is_my_key=\"this is the value\"")
  }
}
