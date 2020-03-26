import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class BlackboardClient implements BlackboardVars{

	private WebDriver driver;


	public BlackboardClient() {
		this(CHROMEDRIVER_PATH);
	}

	public BlackboardClient(String chromeDriverPath) {

		try {
			System.setProperty("webdriver.chrome.driver", chromeDriverPath);

			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors", "--silent");

			driver = new ChromeDriver(options);

		} catch(SessionNotCreatedException e) {
			System.out.println("\nProvided chromedriver is incompatible with your system's chrome browser."
					+ "\nPlease download appropriate version of chromedriver for your computer here:"
					+ "\n\thttps://sites.google.com/a/chromium.org/chromedriver/downloads");
			System.exit(0);
		}
	}

	public void login(String username, String password) {

		//redirects if not logged in
		driver.get(HOME_PAGE);


		WebElement form = driver.findElement(By.xpath(FORM_XPATH));

		WebElement username_Element = form.findElement(By.name("username"));
		username_Element.sendKeys(username);

		WebElement password_Element = form.findElement(By.name("password"));
		password_Element.sendKeys(password);

		form.submit();


		if(!driver.getCurrentUrl().contentEquals(HOME_PAGE)) {
			System.out.println("Incorrect username and password combo");
			System.exit(0);
		}

		driver.get(BLACKBOARD_HOME);
	}

	public void download(String url, String destination) {
		driver.get(url);

		String goal_as_xml = driver.getPageSource();
		Document doc = Jsoup.parse(goal_as_xml);
		Elements sections = doc
				//searches for <li with hrefs and an attribute 'alt' that equals File
				.select("li > a[href]:has([alt=File])");

		//attachment: all downloadable files found on page
		for(Element attachment: sections) {

			//get assignment which attachment is associated with
			Element assignment = attachment.parents()
					.select("[class=clearfix read]").first();
			assignment = assignment
					.select("div[class = item clearfix]").first();
			String folder = assignment.text() + "\\";


			String href = attachment.attr("href");
			String site = BLACKBOARD_DOMAIN + href;
			String name = attachment.text();

			//issues with path
			name = name.replace('\\', '-');
			folder.replace('\\', '-');
			name = name.replace('/', '-');
			folder = folder.replace('/', '-');

			try {
				driver.get(url);
				download(site, destination, folder, name, driver.manage().getCookies());
				System.out.println("\t" +name + " done for: " + assignment.text());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Finished");

	}

	private void download(String site, String destination, String folder, String name, Set<Cookie> set) 
			throws MalformedURLException, IOException {

		URL url = new URL(site);
		urlSetup(url);

		//couldn't find location where file was stored on blackboard server
		//for some reason it showed up in the exception message...
		String location = null;
		try {
			org.jsoup.Connection.Response res = Jsoup
					.connect(site)
					.method(Method.GET)
					.execute();
		} catch(HttpStatusException e) {
			location = e.getUrl();
		}

		//cookie header holds all session info. 
		//necessary for authentication
		List<String> cookies = url.openConnection().getHeaderFields()
				.get("Set-Cookie");

		url = new URL(location);
		urlSetup(url);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		//copy cookies over to new connection
		for (String cookie : cookies) {
			connection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
		}

		//add cookies to request header
		Iterator<Cookie> it = set.iterator();
		while(it.hasNext()) {
			Cookie c = it.next(); //all cookies kept under a single request header
			connection.addRequestProperty("Cookie", c.getName() + "=" + c.getValue());
		}

		//send stream reference and download files to given destination
		downloadFiles(connection.getInputStream(), destination + folder, name);

	}

	//reads from stream and writes files to given destination directory
	private void downloadFiles(InputStream stream, String destination, String fileName) throws IOException {
		BufferedInputStream in = new BufferedInputStream(stream);
		File file = new File(destination);

		if(!file.exists()) file.mkdir();

		FileOutputStream out = new FileOutputStream(new File(file.getAbsolutePath() + "\\" + fileName));
		byte dataBuffer[] = new byte[1024];
		int bytesRead;
		while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
			out.write(dataBuffer, 0, bytesRead);
		}

		in.close();
		out.close();
	}

	//default settings for URL
	private void urlSetup(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoInput(true);
		connection.setUseCaches(true);
		connection.setRequestMethod("GET");
		connection.setFollowRedirects(true);
	}

	//end session
	public void quit() throws IOException {
		driver.quit();
		
		//chromedriver isn't releasing from memory even after quit
		Runtime.getRuntime().exec("taskkill /f /im " + CHROMEDRIVER_PATH);
	}

}
