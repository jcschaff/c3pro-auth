package edu.uconn.c3pro.server.auth.applestore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.uconn.c3pro.server.auth.config.AppConfig;
import edu.uconn.c3pro.server.auth.services.AppleReceiptVerifier;

@Service
@Profile("default")
public class AppleReceiptVerifierApi implements AppleReceiptVerifier {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String JSON_TAG_SANDBOX = "sandbox";
	private static final String JSON_TAG_RECEIPT = "receipt-data";

	private static final String APPLE_JSON_KEY_STATUS = "status";
	private static final String APPLE_JSON_KEY_RECEIPT = "receipt";
	private static final String APPLE_JSON_KEY_RECEIPT_BID = "bid";
	private static final String APPLE_JSON_KEY_BUNDLE = "bundle_id";
	private static final String JSON_REQUEST_APPLE = "{\n" + "  \"" + JSON_TAG_RECEIPT + "\":\"%s\" " + "}";

	@Autowired
	private Environment env;

	@Override
	public boolean verifyReceipt(String receipt) throws Exception {
		logger.info("Validating Apple Receipt");
		logger.info(receipt);
		if (receipt.equals("NO-APP-RECEIPT"))
			return true;
		
		int status = validateAppleReceipt(receipt, env.getProperty(AppConfig.APP_IOS_VERIF_ENDPOINT));
		System.out.println("Returned code: " + status);
		if (status == 21007) {
			// It means we have a receipt from a test environment
			status = validateAppleReceipt(receipt, env.getProperty(AppConfig.APP_IOS_VERIF_TEST_ENDPOINT));
			System.out.println("Returned code: " + status);
		}
		return (status == 0);
	}

	private int validateAppleReceipt(String receipt, String urlStr) throws Exception {
		String jsonReq = String.format(JSON_REQUEST_APPLE, receipt);
		URL url = new URL(urlStr);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-type", "application/json");
		con.setRequestProperty("Content-Length", Integer.toString(jsonReq.getBytes().length));
		con.getOutputStream().write(jsonReq.getBytes());
		con.getOutputStream().flush();
		con.getOutputStream().close();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = in.readLine()) != null) {
			sb.append(line);
		}
		con.getInputStream().close();

		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(sb.toString());

		JsonObject obj = element.getAsJsonObject();
		int status = obj.get(APPLE_JSON_KEY_STATUS).getAsInt();
		boolean ret = false;
		if (status == 0) {
			JsonObject receiptJSON = obj.get(APPLE_JSON_KEY_RECEIPT).getAsJsonObject();
			String bid = null;
			try {
				bid = receiptJSON.get(APPLE_JSON_KEY_BUNDLE).getAsString();
				ret = bid.trim().toLowerCase().equals(env.getProperty(AppConfig.APP_IOS_ID).trim().toLowerCase());
				if (ret)
					status = 0;
			} catch (RuntimeException e) {
				logger.warn(APPLE_JSON_KEY_BUNDLE + " json field not found");
				ret = env.getProperty(AppConfig.APP_IOS_VERIF_ENDPOINT).contains("sandbox");
				if (ret)
					status = 0;
			}
			if (ret) {
				logger.info("Receipt validated against Apple servers");
			} else {
				logger.warn("Receipt status 0, but iOS app id not valid:" + bid);
			}
		} else {
			logger.info("Apple receipt status:" + status);
		}
		return status;
	}

}
