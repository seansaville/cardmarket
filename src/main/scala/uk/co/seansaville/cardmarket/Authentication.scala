package uk.co.seansaville.cardmarket

import java.net.URLEncoder
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import scala.util.Random

case class Credentials(
  accessToken: String,
  accessSecret: String,
  appToken: String,
  appSecret: String
)

case class OAuthHeader(
  realm: String,
  version: String,
  timestamp: String,
  nonce: String,
  consumerKey: String,
  token: String,
  signatureMethod: String,
  signature: String
) {
  def asString: String = {
    "OAuth " +
      OAuthHeader.headerPair("realm", realm) +
      OAuthHeader.headerPair("oauth_version", version) +
      OAuthHeader.headerPair("oauth_timestamp", timestamp) +
      OAuthHeader.headerPair("oauth_nonce", nonce) +
      OAuthHeader.headerPair("oauth_consumer_key", consumerKey) +
      OAuthHeader.headerPair("oauth_token", token) +
      OAuthHeader.headerPair("oauth_signature_method", signatureMethod) +
      OAuthHeader.headerPair("oauth_signature", signature, last = true)
  }
}

object OAuthHeader {
  def headerPair(key: String, value: String, last: Boolean = false): String = {
    key + "=" + "\"" + value + "\"" + (if (!last) ", " else "")
  }
}

object Authentication {
  private def buildBaseString(request: String, realm: String): String = {
    request + "&" + URLEncoder.encode(realm, "UTF-8") + "&"
  }

  private def buildParameterString(oauthP: Map[String, String], urlP: Map[String, String]): String = {
    val merged = (oauthP.toSeq ++ urlP.toSeq).sorted
    val params = for {(name, value) <- merged} yield name + "=" + value
    URLEncoder.encode(params.mkString("&"), "UTF-8")
  }

  private def buildSignature(request: String, signingKey: String): String = {
    Base64.getEncoder.encodeToString(sha1Hash(request.getBytes, signingKey.getBytes))
  }

  private def buildSigningKey(credentials: Credentials): String = {
    val appSecret = URLEncoder.encode(credentials.appSecret, "UTF-8")
    val accessSecret = URLEncoder.encode(credentials.accessSecret, "UTF-8")
    appSecret + "&" + accessSecret
  }

  private def generateNonce(length: Int): String = {
    val randoms = for (_ <- 1 to length) yield Random.nextInt(16).toHexString
    randoms.mkString
  }

  private def generateTimestamp(): String = (System.currentTimeMillis() / 1000).toString

  private def sha1Hash(bytes: Array[Byte], secret: Array[Byte]): Array[Byte] = {
    val sha1 = Mac.getInstance("HmacSHA1")
    sha1.init(new SecretKeySpec(secret, sha1.getAlgorithm))
    sha1.doFinal(bytes)
  }

  private def urlToRealmAndParams(url: String): (String, Map[String, String]) = {
    // Split a URL into the base and, optionally, the parameters
    def splitUrl(url: String): (String, Option[String]) = {
      val regex = "(.*)\\?(.*)".r
      val matched = regex.findFirstMatchIn(url)
      matched match {
        case Some(m) => (m.group(1), Some(m.group(2)))
        case None    => (url, None)
      }
    }

    // Split a parameter into a (name, value) pair
    def splitParameter(param: String): (String, String) = {
      val regex = "(.*)=(.*)".r
      val regex(name, value) = param
      (name, value)
    }

    val (realm, params) = splitUrl(url)
    val parameterMap = params match {
      case None    => Map[String, String]()
      case Some(p) => p.split("&").map(splitParameter).toMap
    }
    (realm, parameterMap)
  }

  def buildOAuthHeader(credentials: Credentials, url: String, request: String,
      getNonce: Int => String = generateNonce,
      getTimestamp: () => String = generateTimestamp): OAuthHeader = {
    val nonce = getNonce(32)
    val signatureMethod = "HMAC-SHA1"
    val timestamp = getTimestamp()
    val version = "1.0"

    // Split the URL into the base URL and its parameters
    val (realm, urlParameters) = urlToRealmAndParams(url)

    // Base string with the request type and the realm
    val baseString = buildBaseString(request, realm)

    // Build a map with all of the OAuth-specific parameters
    val oAuthParameters = Map("oauth_consumer_key" -> credentials.appToken,
                              "oauth_nonce" -> nonce,
                              "oauth_signature_method" -> signatureMethod,
                              "oauth_timestamp" -> timestamp,
                              "oauth_token" -> credentials.accessToken,
                              "oauth_version" -> version)

    // Concatenate the OAuth parameters and the URL parameters into a single string
    val parameterString = buildParameterString(oAuthParameters, urlParameters)

    // Build the signing key and then generate the signature
    val signingKey = buildSigningKey(credentials)
    val signature = buildSignature(baseString + parameterString, signingKey)

    OAuthHeader(realm,
                version,
                timestamp,
                nonce,
                credentials.appToken,
                credentials.accessToken,
                signatureMethod,
                signature)
  }
}
