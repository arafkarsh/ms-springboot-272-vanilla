/**
 * (C) Copyright 2021 Araf Karsh Hamid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fusion.air.microservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map.Entry;
import java.util.function.Function;


/**
 * 
 * @author arafkarsh
 *
 */
@Service
public final class JsonWebTokenValidator {

	// Set Logger -> Lookup will automatically determine the class name.
	//  private static final Logger log = getLogger(lookup().lookupClass());

	/**
	 * Initialize the JWT with the Signature Algorithm based on Secret Key or Public / Private Key
	 */
	public JsonWebTokenValidator() {
	}

	// =============================================================================================
	// Token Data
	// =============================================================================================

	/**
	 * Validate User Id with Token
	 *
	 * @param _userId
	 * @param _token
	 * @return
	 */
	public boolean validateToken(String _userId, TokenData _token) {
		return (!isTokenExpired(_token) &&
				getSubjectFromToken(_token).equals(_userId));
	}

	/**
	 * Returns True if the Token is expired
	 *
	 * @param _token
	 * @return
	 */
	public boolean isTokenExpired(TokenData _token) {
		return getExpiryDateFromToken(_token).before(new Date());
	}

	/**
	 * Get the User / Subject from the Token
	 *
	 * @param _token
	 * @return
	 */
	public String getSubjectFromToken(TokenData _token) {
		return getClaimFromToken(_token, Claims::getSubject);
	}

	/**
	 * Get the Expiry Date of the Token
	 *
	 * @param _token
	 * @return
	 */
	public Date getExpiryDateFromToken(TokenData _token) {
		return getClaimFromToken(_token, Claims::getExpiration);
	}

	/**
	 * Token Should not be used before this Date.
	 *
	 * @param _token
	 * @return
	 */
	public Date getNotBeforeDateFromToken(TokenData _token) {
		return getClaimFromToken(_token, Claims::getNotBefore);
	}
	/**
	 * Get the Token Issue Date
	 *
	 * @param _token
	 * @return
	 */
	public Date getIssuedAtFromToken(TokenData _token) {
		return getClaimFromToken(_token, Claims::getIssuedAt);
	}

	/**
	 * Get the Issuer from the Token
	 *
	 * @param _token
	 * @return
	 */
	public String getIssuerFromToken(TokenData _token) {
		return getClaimFromToken(_token, Claims::getIssuer);
	}

	/**
	 * Get the Audience from the Token
	 *
	 * @param _token
	 * @return
	 */
	public String getAudienceFromToken(TokenData _token) {
		return getClaimFromToken(_token, Claims::getAudience);
	}

	public String getUserRoleFromToken(TokenData token) {
		Claims claims = getAllClaims(token);
		String role = (String) claims.get("rol");
		return (role == null) ? "Public" : role;
	}

	public <T> T getClaimFromToken(TokenData _token,
								   Function<Claims, T> _claimsResolver) {
		return _claimsResolver.apply(getAllClaims(_token));
	}

	public Claims getAllClaims(TokenData _token) {
		return (Claims) getJws(_token).getBody();
	}

	public Jws getJws(TokenData _token) {
		return Jwts.parserBuilder()
				.setSigningKey(_token.getValidatoryKey())
				.requireIssuer(_token.getIssuer())
				.build()
				.parseClaimsJws(_token.getToken());
	}

	/**
	 * Return Payload as JSON String
	 *
	 * @param _token
	 * @return
	 */
	public String getPayload(TokenData _token) {
		StringBuilder sb = new StringBuilder();
		Claims claims = getAllClaims(_token);
		int x=1;
		int size=claims.size();
		sb.append("{");
		for(Entry<String, Object> claim : claims.entrySet()) {
			if(claim != null) {
				sb.append("\""+claim.getKey()+"\": \"").append(claim.getValue());
				sb.append("\"");
				if(x<size) {
					sb.append(",");
				}
			}
			x++;
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Print Token Stats
	 * @param token
	 */
	public void tokenStats(TokenData token) {
		tokenStats(token, true, true);
	}

    /**
     * Print Token Stats
	 * @param token
	 * @param showClaims
	 */
	public void tokenStats(TokenData token,  boolean showClaims) {
		tokenStats(token, showClaims, false);
	}

	/**
	 * Print Token Stats
	 * @param token
	 * @param showClaims
	 * @param showPayload
	 */
    public void tokenStats(TokenData token, boolean showClaims, boolean showPayload) {
		System.out.println("-------------- aaa.bbb.ccc -------------------");
		System.out.println(token);
		System.out.println("-------------- ----------- -------------------");
		System.out.println("Subject  = "+getSubjectFromToken(token));
		System.out.println("Audience = "+getAudienceFromToken(token));
		System.out.println("Issuer   = "+getIssuerFromToken(token));
		System.out.println("IssuedAt = "+getIssuedAtFromToken(token));
		System.out.println("Expiry   = "+getExpiryDateFromToken(token));
		System.out.println("Expired  = "+isTokenExpired(token));
		System.out.println("----------------------------------------------");
		Jws jws = getJws(token);

		System.out.println("Header     : " + jws.getHeader());
		System.out.println("Body       : " + jws.getBody());
		System.out.println("Signature  : " + jws.getSignature());
		if(showClaims) {
			Claims claims = getAllClaims(token);
			int x = 1;
			for (Entry<String, Object> o : claims.entrySet()) {
				System.out.println(x + "> " + o);
				x++;
			}
		}
		if(showPayload) {
			System.out.println("----------------------------------------------");
			System.out.println("Payload=" + getPayload(token));
			System.out.println("----------------------------------------------");
		}

    }

	/**
	 * Returns Expiry Time in Days:Hours:Mins
	 * @param _time
	 * @return
	 */
	public static String printExpiryTime(long _time) {
		String ms="0", hs="0", ds="0";
		long m = _time / (1000 * 60);
		long h = _time / (1000 * 60 * 60);
		long d = _time / (1000 * 60 * 60 * 24);
		if(m > 59) { m = m-(h*60); }
		if(h > 23) { h = h-(d*24);}
		ms = (m<10) ? ms + m : ""+m;
		hs = (h<10) ? hs + h : ""+h;
		ds = (d<10) ? ds + d : ""+d;
		return ds + ":" + hs + ":" + ms;
	}

	/**
	 * Only for Testing from Command Line
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("===============================================================================");
		System.out.println("Generate Json Web Tokens Based on SECRET KEYS");
		System.out.println("===============================================================================");
		// testJWTCreation(JsonWebTokenValidator.SECRET_KEY);
		System.out.println("===============================================================================");
		System.out.println("Generate Json Web Tokens Based on PUBLIC/PRIVATE KEYS");
		System.out.println("===============================================================================");
		// testJWTCreation(JsonWebTokenValidator.PUBLIC_KEY);
		System.out.println("===============================================================================");
	}

	/**
	 * Test JWT Creation
	 * @param _tokenType
	 */
	/**
	protected static void testJWTCreation(int _tokenType) {
		// Default Algo Secret Key is HS512 = Hmac with SHA-512
		// for Public / Private Key is RS256
		JsonWebTokenValidator jsonWebToken = new JsonWebTokenValidator();

		long tokenAuthExpiry = JsonWebTokenValidator.EXPIRE_IN_FIVE_MINS;
		long tokenRefreshExpiry = JsonWebTokenValidator.EXPIRE_IN_THIRTY_MINS;

		String subject	 = "jane.doe";
		String issuer    = "fusion-air";

		Map<String, Object> claims = new HashMap<>();
		claims.put("aud", "generic");
		claims.put("jti", UUID.randomUUID().toString());
		claims.put("rol", "User");
		claims.put("iss", issuer);
		claims.put("sub", subject);

		HashMap<String,String> tokens = jsonWebToken
				.init(_tokenType)
				.setTokenAuthExpiry(tokenAuthExpiry)
				.setTokenRefreshExpiry(tokenRefreshExpiry)
				.generateTokens(subject, issuer, claims, claims);

		String token = tokens.get("token");
		String refresh = tokens.get("refresh");
		System.out.println("Token Expiry in Days:or:Hours:or:Mins  "+ JsonWebTokenValidator.printExpiryTime(tokenAuthExpiry));
		jsonWebToken.tokenStats(token, false, false);

		System.out.println("Refresh Token Expiry in Days:or:Hours:or:Mins "+ JsonWebTokenValidator.printExpiryTime(tokenRefreshExpiry));
		jsonWebToken.tokenStats(refresh, false, false);

	}
	 */


}
