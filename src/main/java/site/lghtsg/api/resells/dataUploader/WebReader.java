package site.lghtsg.api.resells.dataUploader;


import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.resells.dataUploader.model.Resell;
import site.lghtsg.api.resells.dataUploader.model.ResellTodayTrans;
import site.lghtsg.api.resells.dataUploader.model.ResellTransaction;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WebReader {

    private final ResellUploadDao resellUploadDao;

    public WebReader(ResellUploadDao resellUploadDao) {
        this.resellUploadDao = resellUploadDao;
    }

    public BaseResponse<String> uploadResellInfo() {
        ChromeOptions options = new ChromeOptions();
        String path = "/usr/lib/chromium-browser/chromedriver";
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments("--disable-popup-blocking");       //팝업안띄움
        options.addArguments("--headless");                       //브라우저 안띄움
        options.addArguments("--disable-gpu");            //gpu 비활성화
        options.addArguments("--blink-settings=imagesEnabled=false"); //이미지 다운 안받음
        options.addArguments("--single-process");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-debugging-port=9222");
        WebDriver driver = new ChromeDriver(options);

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

            //스크래핑할 브랜드 url로 이동
            String url = "https://kream.co.kr/search?category_id=2";
            driver.navigate().to(url);
            Thread.sleep(1000);


            //스크롤 내리기
            int lastHeight = 0;
            WebElement element = driver.findElement(By.className("product_card"));
            var stTime = new Date().getTime();
            while (new Date().getTime() < stTime + 900000) {
                Thread.sleep(2000);
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)", element);

                int newHeight = Integer.parseInt(((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight").toString());
                if (newHeight == lastHeight) {
                    System.out.println("종료");
                    break;
                }

                lastHeight = newHeight;
                System.out.println(lastHeight);
            }
            System.out.println("종료2");


            //urlList 스크래핑
            List<WebElement> elements = driver.findElements(By.className("product_card"));
            List<String> urlList = new ArrayList<>();

            for (int i = 0; i < elements.size(); i++) {
                String productUrl = elements.get(i).findElement(By.tagName("a")).getAttribute("href");
                urlList.add(productUrl);
            }
            System.out.println("size = " + urlList.size());
            Thread.sleep(1000);

            //스크래핑 시작
            while (!urlList.isEmpty()) {
                driver.get(urlList.get(0));
                //상품명, 브랜드, 이미지 url, 모델번호, 발매일자, 발매가, 색상 스크래핑
                WebElement name = driver.findElement(By.className("sub_title"));
                WebElement brand = driver.findElement(By.className("brand"));
                List<WebElement> imageUrlList = driver.findElements(By.className("slide_item"));
                List<WebElement> productInfo = driver.findElements(By.tagName("dd"));

                if (productInfo.size() == 0 || imageUrlList.size() == 0 || name == null || brand == null) {
                    urlList.add(urlList.get(0));
                } else {
                    Resell resell = new Resell();
                    resell.setName(name.getText());
                    resell.setReleasedPrice(productInfo.get(3).getText());
                    resell.setReleasedDate(productInfo.get(1).getText());
                    resell.setColor(productInfo.get(2).getText());
                    resell.setBrand(brand.getText());
                    resell.setProductNum(productInfo.get(0).getText());
                    resell.setImage1(imageUrlList.get(0).findElement(By.tagName("img")).getAttribute("src"));
                    int productCode = Integer.parseInt(driver.getCurrentUrl().replaceAll("[^0-9]", ""));
                    resell.setProductCode(productCode);

                    System.out.println(productCode);

                    int checkDuplicated = resellUploadDao.checkDuplicated(productCode);

                    if (checkDuplicated == 1) {
                        urlList.remove(0);
                        System.out.println("중복ㅋㅋ");
                        System.out.println("------");
                        continue;
                    }

                    if (imageUrlList.size() >= 2) {
                        resell.setImage2(imageUrlList.get(1).findElement(By.tagName("img")).getAttribute("src"));
                    }

                    if (imageUrlList.size() >= 3) {
                        resell.setImage3(imageUrlList.get(2).findElement(By.tagName("img")).getAttribute("src"));
                    }
                    resell.setIconImageIdx(1L);

                    //거래 내역 더보기 클릭 (거래가 존재하는지 확인)
                    try {
                        driver.findElement(By.xpath("//*[@id=\"panel1\"]/a")).click();
                    } catch (Exception e) {
                        urlList.remove(0);
                        continue;
                    }
                    resellUploadDao.uploadResell(resell);
                    Thread.sleep(1000);
                }
                urlList.remove(0);
            }
            return new BaseResponse<>(" 리셀 데이터 업로드 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>("리셀 데이터 업로드 실패");
        } finally {
            driver.quit();
        }
    }

    public BaseResponse<String> uploadResellTrans(int startResellIdx, int lastResellIdx) {

        ChromeOptions options = new ChromeOptions();

        String path = "/usr/lib/chromium-browser/chromedriver";
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments("--disable-popup-blocking");       //팝업안띄움
        options.addArguments("--headless");                       //브라우저 안띄움
        options.addArguments("--disable-gpu");            //gpu 비활성화
        options.addArguments("--blink-settings=imagesEnabled=false"); //이미지 다운 안받음
        options.addArguments("--single-process");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-debugging-port=9222");
        WebDriver driver = new ChromeDriver(options);

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

            for (; startResellIdx <= lastResellIdx; startResellIdx++) {
                int productCode = resellUploadDao.getProductCode(startResellIdx);

                Thread.sleep(1000);

                String productUrl = "https://kream.co.kr/products/" + productCode;
                driver.get(productUrl);

                try {
                    driver.findElement(By.xpath("//*[@id=\"panel1\"]/a")).click();
                } catch (Exception e) {
                    driver.get("https://kream.co.kr/login");
                    try {
                        driver.findElement(By.xpath("//*[@id=\"__layout\"]/div/div[2]/div[1]/div/div[1]/div/input")).sendKeys("wnsdud6969@naver.com");
                        driver.findElement(By.xpath("//*[@id=\"__layout\"]/div/div[2]/div[1]/div/div[2]/div/input")).sendKeys("qkrwnsdud123!");

                        //버튼 클릭
                        driver.findElement(By.xpath("//*[@id=\"__layout\"]/div/div[2]/div[1]/div/div[3]/a")).click();
                        System.out.println("로그인 성공 = " + driver.getCurrentUrl());
                        Thread.sleep(1000);
                    } catch (Exception ea) {
                        startResellIdx--;
                        continue;
                    }
                }

                Thread.sleep(1000);

                WebElement transaction = driver.findElement(By.className("price_body"));
                List<WebElement> transaction1 = new ArrayList<>();
                System.out.println("시작");


                int transactionSize;
                int lastHeight = 0;
                int newHeight = 0;

                while (true) {
                    Thread.sleep(5000);

                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight", transaction);
                        newHeight = Integer.parseInt(((JavascriptExecutor) driver).executeScript("return arguments[0].scrollTop", transaction).toString());
                    } catch (Exception e) {
                        driver.get("https://kream.co.kr/login");
                        try {
                            driver.findElement(By.xpath("//*[@id=\"__layout\"]/div/div[2]/div[1]/div/div[1]/div/input")).sendKeys("wnsdud6969@naver.com");
                            driver.findElement(By.xpath("//*[@id=\"__layout\"]/div/div[2]/div[1]/div/div[2]/div/input")).sendKeys("qkrwnsdud123!");

                            //버튼 클릭
                            driver.findElement(By.xpath("//*[@id=\"__layout\"]/div/div[2]/div[1]/div/div[3]/a")).click();
                            System.out.println("로그인 성공 = " + driver.getCurrentUrl());
                            Thread.sleep(1000);
                        } catch (Exception ea) {
                            continue;
                        }
                    }

                    if (newHeight == lastHeight) {
                        System.out.println("종료3");
                        break;
                    }

                    lastHeight = newHeight;
                    System.out.println(lastHeight);
                }

                transaction1 = driver.findElements(By.className("list_txt"));
                transactionSize = transaction1.size() / 3;
                System.out.println(transactionSize);

                Thread.sleep(1000);

                //거래가격, 거래날짜 저장
                List<List<String>> priceList = new ArrayList<>();
                for (int i = transaction1.size() - 2; i >= 1; i -= 3) {
                    List<String> temp = new ArrayList<>();
                    temp.add(transaction1.get(i).getText());
                    temp.add(transaction1.get(i + 1).getText());
                    priceList.add(temp);
                }

                //거래가 1개면 패스
                if (priceList.size() <= 1) {
                    continue;
                }

                String lastDay = "";

                for (List<String> list : priceList) {
                    if (lastDay.equals(list.get(1))) {
                        continue;
                    }

                    String temp = list.get(0);
                    int price = Integer.parseInt(temp.replaceAll("[^0-9]", ""));

                    ResellTransaction resellTransaction = new ResellTransaction(startResellIdx, price, list.get(1));

                    lastDay = list.get(1);

                    resellUploadDao.uploadResellTransaction(resellTransaction);
                }
                System.out.println("————————————");
            }

            return new BaseResponse<>(" 리셀 데이터 업로드 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>("리셀 데이터 업로드 실패");
        } finally {
            driver.quit();
        }
    }

//    @Async
//    @Scheduled(cron = "0 0 1-23 * * *")
    public BaseResponse<String> updateByHour() {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        List<Integer[]> resellIdxAndProductCodeList = resellUploadDao.getResellIdxAndProductCode();
        List<ResellTodayTrans> resellTodayTransList = new ArrayList<>();

        // 현재 날짜/시간
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd/HH");
        String today = now.format(formatter);


        try {
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            options.addArguments("--disable-popup-blocking");       //팝업안띄움
            //options.addArguments("headless");                       //브라우저 안띄움
            options.addArguments("--disable-gpu");            //gpu 비활성화
            options.addArguments("--blink-settings=imagesEnabled=false"); //이미지 다운 안받음
            options.addArguments("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
            //WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            int limit = 0;

            while (!resellIdxAndProductCodeList.isEmpty()) {

                //int lastTransPrice = resellUploadDao.getLastTransactionPrice(resellIdxAndProductCodeList.get(0)[1]);
                int price;
                int resellIdx = resellIdxAndProductCodeList.get(0)[0];
                Thread.sleep(1000);
                driver.get("https://kream.co.kr/products/" + resellIdxAndProductCodeList.get(0)[1]);

                try {
                    price = Integer.parseInt(driver.findElement(By.className("num")).getText().replaceAll("[^0-9]", ""));
                    Thread.sleep(1000);
/*
                    if (price == lastTransPrice) {
                        String compare = driver.findElement(By.className("fluctuation")).findElement(By.tagName("p")).getText();
                        int idx = compare.indexOf(" ");
                        compare = compare.substring(0, idx);
                        int comparePrice = Integer.parseInt(compare.replaceAll("[^0-9]", ""));
                        System.out.println(comparePrice);
                        //한 시간 전 가격 == 지금 가격 but 증감 가격 != 0 -> 거래 발생 x
                        if (comparePrice != 0) {
                            System.out.println("추가 안 한다~");
                            System.out.println("-------------");
                            resellIdxAndProductCodeList.remove(0);
                            continue;
                        }
                    }*/
                    limit = 0;
                } catch (Exception e) {
                    limit++;

                    if (limit > 3) {
                        resellIdxAndProductCodeList.remove(0);
                        System.out.println("3번 이상 접속 불가");
                        limit = 0;
                    }

                    continue;
                }

                ResellTodayTrans resellTodayTrans = new ResellTodayTrans(resellIdx, price);
                resellTodayTransList.add(resellTodayTrans);

                formatter = DateTimeFormatter.ofPattern("yy/MM/dd");
                today = now.format(formatter);
                System.out.println(today);

                //오늘 거래된 가격들 가져오기
                List<Integer> todayTransactionList = resellUploadDao.getTransactionToday(resellIdxAndProductCodeList.get(0)[0], today);

                if (todayTransactionList.size() == 0) {
                    //resellUploadDao.updateResellTransactionByHour(resellIdx, price, today);
                } else {
                    int todayTotal = 0;

                    for (Integer i : todayTransactionList) {
                        todayTotal += i;
                    }

                    int updatePrice = todayTotal / todayTransactionList.size();

                    resellUploadDao.updateResellTransactionByHour(resellIdx, updatePrice, today);
                }

                resellIdxAndProductCodeList.remove(0);
            }
            formatter = DateTimeFormatter.ofPattern("yy/MM/dd/HH");
            today = now.format(formatter);

            resellUploadDao.updateResellTodayTransByHour(resellTodayTransList, today);
            resellUploadDao.updateLastTransactionIdx();
            resellUploadDao.updateS2LastTransactionIdx();
            return new BaseResponse<>("한 시간 주기 업데이트 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>("한 시간 주기 업데이트 실패");
        } finally {
            driver.quit();
        }
    }

//    @Async
//    @Scheduled(cron = "0 0 0 * * *")
    public BaseResponse<String> updateByDay() {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        List<Integer[]> resellIdxAndProductCodeList = resellUploadDao.getResellIdxAndProductCode();
        List<ResellTodayTrans> resellTodayTransList = new ArrayList<>();

        // 현재 날짜/시간
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd");
        String today = now.format(formatter);

        try {
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            options.addArguments("--disable-popup-blocking");       //팝업안띄움
            //options.addArguments("headless");                       //브라우저 안띄움
            options.addArguments("--disable-gpu");            //gpu 비활성화
            options.addArguments("--blink-settings=imagesEnabled=false"); //이미지 다운 안받음
            options.addArguments("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
            //WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            while (!resellIdxAndProductCodeList.isEmpty()) {
                int price;
                int resellIdx = resellIdxAndProductCodeList.get(0)[0];
                driver.get("https://kream.co.kr/products/" + resellIdxAndProductCodeList.get(0)[1]);

                try {
                    price = Integer.parseInt(driver.findElement(By.className("num")).getText().replaceAll("[^0-9]", ""));
                } catch (Exception e) {
                    resellIdxAndProductCodeList.add(resellIdxAndProductCodeList.get(0));
                    continue;
                }

                Date dDate = new Date();
                dDate = new Date(dDate.getTime() + (1000 * 60 * 60 * 24 * -1));
                SimpleDateFormat dSdf = new SimpleDateFormat("yy/MM/dd", Locale.KOREA);
                String yesterday = dSdf.format(dDate);

                //어제 거래된 가격들 가져오기
                List<Integer> yesterdayTransactionList = resellUploadDao.getTransactionToday(resellIdxAndProductCodeList.get(0)[0], yesterday);

                //전날 거래 마무리
                if (yesterdayTransactionList.size() == 0) {
                    resellUploadDao.updateResellTransactionByHour(resellIdx, price, yesterday);
                } else {
                    int yesterdayTotal = 0;

                    for (Integer i : yesterdayTransactionList) {
                        yesterdayTotal += i;
                    }

                    int updatePrice = yesterdayTotal / yesterdayTransactionList.size();

                    resellUploadDao.updateResellTransactionByHour(resellIdx, updatePrice, yesterday);
                }

                //오늘 거래 시작
                resellUploadDao.startTodayTransaction(resellIdx, price, today);

                formatter = DateTimeFormatter.ofPattern("yy/MM/dd/HH");
                today = now.format(formatter);

                ResellTodayTrans resellTodayTrans = new ResellTodayTrans(resellIdx, price);
                resellTodayTransList.add(resellTodayTrans);

                resellIdxAndProductCodeList.remove(0);
            }
            formatter = DateTimeFormatter.ofPattern("yy/MM/dd/HH");
            today = now.format(formatter);

            resellUploadDao.updateResellTodayTransByHour(resellTodayTransList, today);
            resellUploadDao.updateLastTransactionIdx();
            resellUploadDao.updateS2LastTransactionIdx();
            resellUploadDao.truncateResellYesterdayTrans();
            return new BaseResponse<>("하루 주기 업데이트 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>("하루 시간 주기 업데이트 실패");
        } finally {
            driver.quit();
        }
    }
}