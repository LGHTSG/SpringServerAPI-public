package site.lghtsg.api.resells;


import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.resells.model.GetResellRes;
import site.lghtsg.api.resells.model.GetResellTransactionRes;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.*;

@Repository
public class ResellDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<GetResellRes> getResells(String order){
        String getResellsQuery = "select * from Resell order by resellIdx ";
        if(order.equals("ascending")){
            getResellsQuery += "ASC";
        }

        if (order.equals("descending")){
            getResellsQuery += "DESC";
        }

        return this.jdbcTemplate.query(getResellsQuery,
                (rs, row) -> new GetResellRes(
                        rs.getInt("resellIdx"),
                        rs.getString("name"),
                        calculateChangeOfRate(rs.getInt("resellIdx")).get(0),
                        rs.getInt("releasedPrice"),
                        rs.getString("releasedDate"),
                        rs.getString("color"),
                        rs.getString("brand"),
                        rs.getString("productNum"),
                        calculateChangeOfRate(rs.getInt("resellIdx")).get(1),
                        "최근 거래가 기준",
                        rs.getString("image1"),
                        rs.getString("image2"),
                        rs.getString("image3")
                )
        );
    }

    public List<GetResellRes> getResellsByRate(){
        String getResellsQuery = "select * from Resell";

        return this.jdbcTemplate.query(getResellsQuery,
                (rs, row) -> new GetResellRes(
                        rs.getInt("resellIdx"),
                        rs.getString("name"),
                        calculateChangeOfRate(rs.getInt("resellIdx")).get(0),
                        rs.getInt("releasedPrice"),
                        rs.getString("releasedDate"),
                        rs.getString("color"),
                        rs.getString("brand"),
                        rs.getString("productNum"),
                        calculateChangeOfRate(rs.getInt("resellIdx")).get(1),
                        "최근 거래가 기준",
                        rs.getString("image1"),
                        rs.getString("image2"),
                        rs.getString("image3")
                )
        );
    }

    public GetResellRes getResell(int resellIdx){
        String getResellQuery = "select * from Resell where resellIdx = ?";
        int getResellParams = resellIdx;

        return this.jdbcTemplate.queryForObject(getResellQuery,
                (rs, rowNum) -> new GetResellRes(
                        rs.getInt("resellIdx"),
                        rs.getString("name"),
                        calculateChangeOfRate(rs.getInt("resellIdx")).get(0),
                        rs.getInt("releasedPrice"),
                        rs.getString("releasedDate"),
                        rs.getString("color"),
                        rs.getString("brand"),
                        rs.getString("productNum"),
                        calculateChangeOfRate(rs.getInt("resellIdx")).get(1),
                        "최근 거래가 기준",
                        rs.getString("image1"),
                        rs.getString("image2"),
                        rs.getString("image3")),
                getResellParams);
    }

    public List<GetResellTransactionRes> getResellTransaction(int resellIdx){
        String getResellTransactionQuery = "select * from ResellTransaction where resellIdx = ?";
        int getResellTransactionParams = resellIdx;
        return this.jdbcTemplate.query(getResellTransactionQuery,
                (rs, rowNum) -> new GetResellTransactionRes(
                        rs.getInt("resellIdx"),
                        rs.getInt("price"),
                        rs.getTimestamp("transactionTime")),
                getResellTransactionParams);
    }

    public List<GetResellTransactionRes> getResellTransactionHistory(int resellIdx){
        String getResellTransactionHistoryQuery = "select * from ResellTransaction where resellIdx = ? order by transactionTime desc LIMIT 2";
        int getResellTransactionHistory = resellIdx;
        return this.jdbcTemplate.query(getResellTransactionHistoryQuery,
                (rs, rowNum) -> new GetResellTransactionRes(
                        rs.getInt("resellIdx"),
                        rs.getInt("price"),
                        rs.getTimestamp("transactionTime")),
                getResellTransactionHistory);
    }

    public List<String> calculateChangeOfRate(int resellIdx){
        List<GetResellTransactionRes> resellTransactionHistory = getResellTransactionHistory(resellIdx);
        List<String> result = new ArrayList<>();
        int currentPrice = resellTransactionHistory.get(1).getPrice();
        int latestPrice = resellTransactionHistory.get(0).getPrice();

        double changeOfRate = (double) (currentPrice - latestPrice) / latestPrice * 100;
        String changeOfRateS = String.format("%.1f",changeOfRate);

        if(changeOfRate > 0){
            changeOfRateS = "+" + changeOfRateS;
        }
        result.add(String.valueOf(currentPrice));
        result.add(changeOfRateS);
        return result;
    }

    public void scraping() {
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);

        options.addArguments("--disable-popup-blocking");       //팝업안띄움
        //options.addArguments("headless");                       //브라우저 안띄움
        options.addArguments("--disable-gpu");            //gpu 비활성화
        options.addArguments("--blink-settings=imagesEnabled=false"); //이미지 다운 안받음
        options.addArguments("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            //login 페이지
            driver.get("https://kream.co.kr/login");
            //id, pw 입력
            driver.findElement(By.xpath("//*[@id=\"__layout\"]/div/div[2]/div[1]/div/div[1]/div/input")).sendKeys("wnsdud6969@naver.com");
            driver.findElement(By.xpath("//*[@id=\"__layout\"]/div/div[2]/div[1]/div/div[2]/div/input")).sendKeys("qkrwnsdud123!");
            //버튼 클릭
            driver.findElement(By.xpath("//*[@id=\"__layout\"]/div/div[2]/div[1]/div/div[3]/a")).click();
            System.out.println("로그인 성공 = " + driver.getCurrentUrl());

            Thread.sleep(1000);
            String url = "https://kream.co.kr/brands/nike";
            driver.navigate().to(url);
            Thread.sleep(1000);
            driver.findElement(By.xpath("//*[@id=\"wrap\"]/div[2]/div/div[2]/div[2]/div[2]/div[1]/div[2]/button")).click();
            Thread.sleep(1000);
            driver.findElement(By.xpath("//*[@id=\"wrap\"]/div[2]/div/div[2]/div[2]/div[2]/div[1]/div[2]/ul/li[4]/a")).click();
            Thread.sleep(1000);

            String createResellQuery = "insert into Resell (name, releasedPrice, releasedDate, color, brand, productNum, image1) VALUE (?,?,?,?,?,?,?)"; // 실행될 동적 쿼리문
            String createResellTransactionQuery = "insert into ResellTransaction (resellIdx, price, transactionTime) VALUE (?,?,?)";
            List<WebElement> elements = driver.findElements(By.className("product_card"));

            List<String> urlList = new ArrayList<>();

            for (int i = 0; i < elements.size(); i++) {
                String productUrl = elements.get(i).findElement(By.tagName("a")).getAttribute("href");
                urlList.add(productUrl);
            }

            int resellIdx = 0;
            int max = 0;
            while (!urlList.isEmpty()) {
                driver.get(urlList.get(0));
                WebElement name = driver.findElement(By.className("sub_title"));
                WebElement brand = driver.findElement(By.className("brand"));
                String imageUrl = driver.findElement(By.tagName("img")).getAttribute("src");
                List<WebElement> productInfo = driver.findElements(By.tagName("dd"));
                if (productInfo.size() == 0) {
                    urlList.add(urlList.get(0));
                    max++;
                } else {
                    resellIdx++;
                    System.out.println(resellIdx + "번째");
                    System.out.println(name.getText());
                    System.out.println(brand.getText());
                    System.out.println(productInfo.get(0).getText());
                    System.out.println(productInfo.get(1).getText());
                    System.out.println(productInfo.get(2).getText());
                    System.out.println(productInfo.get(3).getText());
                    System.out.println(imageUrl);

                    Object[] createResellParams = new Object[7];

                    createResellParams[0] =name.getText();
                    createResellParams[1] =productInfo.get(3).getText();
                    createResellParams[2] =productInfo.get(1).getText();
                    createResellParams[3] =productInfo.get(2).getText();
                    createResellParams[4] =brand.getText();
                    createResellParams[5] =productInfo.get(0).getText();
                    createResellParams[6] = imageUrl;

                    this.jdbcTemplate.update(createResellQuery, createResellParams);
                    Thread.sleep(3000);

                    try {
                        driver.findElement(By.xpath("//*[@id=\"panel1\"]/a")).click();
                    } catch (Exception e) {
                        resellIdx--;
                        urlList.remove(0);
                        continue;
                    }

                    Thread.sleep(2000);
                    List<WebElement> transaction = driver.findElements(By.className("list_txt"));

                    //거래가격, 거래날짜 저장
                    List<List<String>> priceList = new ArrayList<>();

                    for (int i = transaction.size() - 2; i >= 1; i -= 3) {
                        List<String> temp = new ArrayList<>();
                        temp.add(transaction.get(i).getText());
                        temp.add(transaction.get(i + 1).getText());
                        priceList.add(temp);
                    }

                    if (priceList.size() <= 1) {
                        resellIdx--;
                        urlList.remove(0);
                        continue;
                    }


                    for (List<String> list : priceList) {
                        String temp = list.get(0);
                        int price = Integer.parseInt(temp.replaceAll("[^0-9]", ""));
                        System.out.println(resellIdx + " - " + price + " - " + list.get(1));

                        Object[] createResellTransactionParams = new Object[]{resellIdx, price, list.get(1)};
                        this.jdbcTemplate.update(createResellTransactionQuery, createResellTransactionParams);
                    }
                    System.out.println("------------------------");
                }
                urlList.remove(0);
            }
            System.out.println(max);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
