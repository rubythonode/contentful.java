package com.contentful.java.cda;

import java.util.Locale;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

import static java.lang.String.format;

/**
 * This class will represent known Contentful exceptions
 */
public class CDAHttpException extends RuntimeException {
  private final Request request;
  private final Response response;

  /**
   * Construct an error response.
   * <p>
   * This constructor will fill the exception with easy accessible values, like
   * {@link #responseCode()}. {@link #responseMessage()}, but also
   * {@link #rateLimitReset()}.
   *
   * @param request  the request issuing the error.
   * @param response the response from the server to this faulty request.
   */
  public CDAHttpException(Request request, Response response) {
    this.request = request;
    this.response = response;
  }

  /**
   * Convert exception to human readable form.
   *
   * @return a string representing this exception.
   */
  @Override
  public String toString() {
    return format(
        Locale.getDefault(),
        "FAILED REQUEST:\n\t%s\n\t↳ Header{%s}\n\t%s\n\t↳ Header{%s}",
        request.toString(),
        headersToString(request.headers()),
        response.toString(),
        headersToString(response.headers()));
  }

  /**
   * @return the response code of the request.
   */
  public int responseCode() {
    return response.code();
  }

  /**
   * @return the message the server returned.
   */
  public String responseMessage() {
    return response.message();
  }

  /**
   * @return the hourly rate limit or -1 if header not send
   */
  public int rateLimitHourLimit() {
    return parseRateLimitHeader(("X-Contentful-RateLimit-Hour-Limit"));
  }

  /**
   * @return the number of remaining requests that can be made for the current hour or -1 if header not send
   */
  public int rateLimitHourRemaining() {
    return parseRateLimitHeader(("X-Contentful-RateLimit-Hour-Remaining"));
  }

  /**
   * @return the per second rate limit or -1 if header not send
   */
  public int rateLimitSecondLimit() {
    return parseRateLimitHeader(("X-Contentful-RateLimit-Second-Limit"));
  }

  /**
   * @return the number of remaining requests that can be made per second or -1 if header not send
   */
  public int rateLimitSecondRemaining() {
    return parseRateLimitHeader(("X-Contentful-RateLimit-Second-Remaining"));
  }

  /**
   * @return the number of seconds until the user can make their next request or -1 if header not send
   */
  public int rateLimitReset() {
    return parseRateLimitHeader(("X-Contentful-RateLimit-Reset"));
  }

  private String headersToString(Headers headers) {
    final StringBuilder builder = new StringBuilder();

    String divider = "";
    for (final String name : headers.names()) {
      final String value = headers.get(name);
      builder.append(divider);
      builder.append(name);
      builder.append(": ");
      builder.append(value);

      if ("".equals(divider)) {
        divider = ", ";
      }
    }

    return builder.toString();
  }

  private int parseRateLimitHeader(String name) {
    try {
      return Integer.parseInt(response.header(name));
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}
