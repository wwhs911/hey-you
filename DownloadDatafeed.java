import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

public class DownloadDatafeed {

	public static void main(String[] args) throws Exception {

		File file = new File("revolve.txt");

		if (!file.exists()) {

			System.out.println("文件不存在，开始下载!这是一个测试");

			System.out.println(new Date());

			Builder builder = new Request.Builder().url("http://server.revolveme.cn/feed/product/mymm").get();

			builder.addHeader("content-type", "text/plain");

			Request downloadRequest = builder.build();

			OkHttpClient client = new OkHttpClient.Builder().writeTimeout(180, TimeUnit.SECONDS).readTimeout(600, TimeUnit.SECONDS).build();

			Response downloadResponse = client.newCall(downloadRequest).execute();

			if (downloadResponse.isSuccessful()) {
				try (FileOutputStream fop = new FileOutputStream(file)) {
					file.createNewFile();

					// get the content in bytes
					byte[] contentInBytes = downloadResponse.body().bytes();

					fop.write(contentInBytes);
					fop.flush();
					fop.close();

					System.out.println("Successfully download: " + "revolve.txt");

				} catch (IOException e) {
					throw new Exception("Failed to download: " + "revolve.txt" + "\nException: " + e.getLocalizedMessage());
				}
			} else {
				throw new Exception("Failed GET: " + "http://server.revolveme.cn/feed/product/mymm" + "\nFilename: " + "revolve.txt" + "\nException: " + downloadResponse.message());
			}

			System.out.println(new Date());
		}

		// brand

		List<String> brandList = new ArrayList<String>();
		HashMap<String, HashSet<String>> skuMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> styleMap = new HashMap<String, HashSet<String>>();

		HashMap<String, HashMap<String, HashSet<String>>> genderMap = new HashMap<String, HashMap<String, HashSet<String>>>();

		File brandFile = new File("brand.txt");
		BufferedReader brandBR = new BufferedReader(new FileReader(brandFile));

		String brandLineString = brandBR.readLine();

		while (brandLineString != null && !brandLineString.isEmpty()) {
			String brand = brandLineString;
			if (!skuMap.containsKey(brand)) {
				skuMap.put(brand, new HashSet<String>());
			}
			if (!styleMap.containsKey(brand)) {
				styleMap.put(brand, new HashSet<String>());
			}
			if (!genderMap.containsKey(brand)) {
				genderMap.put(brand, new HashMap<String, HashSet<String>>());
			}

			brandList.add(brandLineString);

			// go next line
			brandLineString = brandBR.readLine();

		}

		brandBR.close();

		//

		BufferedReader br = new BufferedReader(new FileReader(file));

		// create return object
		// List<String> result = new ArrayList<String>();

		String lineString = br.readLine();
		lineString = br.readLine();

		HashSet<String> gender = new HashSet<>();
		HashSet<String> category = new HashSet<>();

		while (lineString != null && !lineString.isEmpty()) {
			// String trimLineString = lineString.trim();
			// result.add(trimLineString);

			String[] lineDatas = lineString.split("\t");
			// System.out.println(lineDatas[1]);

			// if (lineDatas[1].equals("Frye")) {
			// count++;
			// }

			String brandName = lineDatas[1];
			String skuCode = lineDatas[0];
			String styleCode = skuCode.substring(0, skuCode.lastIndexOf("-"));

			if (skuMap.get(brandName) != null) {
				skuMap.get(brandName).add(skuCode);
			}
			if (styleMap.get(brandName) != null) {
				styleMap.get(brandName).add(styleCode);
			}
			if (genderMap.get(brandName) != null) {
				HashMap<String, HashSet<String>> genderRel = genderMap.get(brandName);

				String genderString = lineDatas[7] + "-" + lineDatas[8];
				if (genderRel.get(genderString) == null) {
					genderRel.put(genderString, new HashSet<String>());
				} else {
					genderRel.get(genderString).add(styleCode);
				}
			}

			// gender.add(lineDatas[7] + "\t" + lineDatas[8]);
			category.add(lineDatas[18]);
			// go next line
			lineString = br.readLine();

			// if (count > 10) {
			// break;
			// }
		}

		for (Iterator<String> iterator = brandList.iterator(); iterator.hasNext();) {
			String brand = iterator.next();
			System.out.println(brand + "\t" + styleMap.get(brand).size() + "\t" + skuMap.get(brand).size() + "\t"
					+ (genderMap.get(brand).get("Adult-Female") == null ? 0 : genderMap.get(brand).get("Adult-Female").size()) + "\t"
					+ (genderMap.get(brand).get("Adult-Male") == null ? 0 : genderMap.get(brand).get("Adult-Male").size()) + "\t"
					+ (genderMap.get(brand).get("Adult-Unisex") == null ? 0 : genderMap.get(brand).get("Adult-Unisex").size()) + "\t"
					+ (genderMap.get(brand).get("Kids-Female") == null ? 0 : genderMap.get(brand).get("Kids-Female").size()) + "\t"
					+ (genderMap.get(brand).get("Kids-Male") == null ? 0 : genderMap.get(brand).get("Kids-Male").size()) + "\t"
					+ (genderMap.get(brand).get("Toddler-Female") == null ? 0 : genderMap.get(brand).get("Toddler-Female").size()) + "\t"
					+ (genderMap.get(brand).get("Toddler-Male") == null ? 0 : genderMap.get(brand).get("Toddler-Male").size()));
		}


		br.close();

	}

}
