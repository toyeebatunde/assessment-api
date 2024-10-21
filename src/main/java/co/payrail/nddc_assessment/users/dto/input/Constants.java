package co.payrail.nddc_assessment.users.dto.input;

public class Constants {

    public static final long ACCESS_TOKEN_VALIDITY_SECONDS = 120 * 120 * 60;
    public static final String SIGNING_KEY = "UGF5cmFpbCBhZ2VuY3kgc2NlY3JldCBmb3IgcGF5cmFpbCBkYXNoYm9hcmQgYXBp";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String AUTHORITIES_KEY = "permissions";
}