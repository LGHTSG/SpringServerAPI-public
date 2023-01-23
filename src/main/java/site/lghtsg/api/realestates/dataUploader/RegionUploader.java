package site.lghtsg.api.realestates.dataUploader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.lghtsg.api.realestates.dataUploader.model.RegionName;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@RequiredArgsConstructor
@Service
public class RegionUploader {

    private final RealEstateUploadDao realEstateUploadDao;

    private final String fileName = "C:\\Users\\2High\\Downloads\\법정동코드 전체자료\\법정동코드 전체자료.txt";

    public String readData() {

        List<HashMap<Integer, String>> regions = new ArrayList<>(4); // 길이가 짧은 것부터 업로드 예정 (parentID 때문에)

        try {

            for (int i = 0; i < 4; i++) {
                regions.add(new HashMap<Integer, String>()); // 지역 ID, 지역명 저장
            }

            // 파일 읽어오기
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "MS949"));

            String line;

            fileReader.readLine(); // 첫 줄(컬럼 이름 표시) 버리기

            while ((line = fileReader.readLine()) != null) {

                String[] row = line.split("[ \t]");

                // 폐지된 지역명, 리 단위 정보는 제외
                if (!isRequired(row)) continue;

                int regionCode = Integer.parseInt(row[0].substring(0, 8));
                String[] nameParts = Arrays.copyOfRange(row, 1, row.length-1);

                String name = String.join(" ", nameParts);

                if (nameParts.length == 5) {
                    for (String part : row) {
                        System.out.println(part);
                    }
                }
                regions.get(nameParts.length-1).put(regionCode, name);
            }
            createRegionName(regions);

            return "success";
        } catch (IOException e) {
            return "파일 읽기 실패";
        }
    }

    private boolean isRequired(String[] parts) {

        boolean isRequired = !(parts[parts.length - 1].equals("폐지")
                || parts[parts.length - 2].endsWith("리")
                || parts[parts.length - 2].endsWith(")"));

        return isRequired;
    }

    public void createRegionName(List<HashMap<Integer, String>> regions) {
        for (int i = 0; i < regions.size(); i++) { // 4번 반복(지역 length별)
            List<RegionName> regionData = new ArrayList<>();

            HashMap<Integer, String> hashMap = regions.get(i);


            for (Map.Entry<Integer, String> pair : hashMap.entrySet()) { // 각 타입의 지역 수만큼 반복
                RegionName regionName = RegionName.builder()
                        .legalCodeId(pair.getKey())
                        .name(pair.getValue())
                        .build();

                // 최상위 지역 (parentId X)은 바로 추가
                if (i == 0) {
                    regionData.add(regionName);
                    continue;
                }

                // 상위 지역 찾기
                List<RegionName> regionsInDB = realEstateUploadDao.getRegions();

                String nameInFile = regionName.getName();

                List<RegionName> parentRegions = new ArrayList<>();// 상위 지역이 여러개일 수 있음 (구 단위, 도 단위 등)

                for (RegionName regionNameInDB : regionsInDB) {
                    String nameInDB = regionNameInDB.getName();

                    if (nameInFile.contains(nameInDB)) { // 속한 지역명을 찾아서
                        parentRegions.add(regionNameInDB);
                    }
                }


                if (i == 1) { // 상위 지역이 1개
                    regionName.setParentId(parentRegions.get(0).getLegalCodeId());
                }

                // 상위 지역이 여러 개 -> 가장 긴 지역명(가장 하위 지역명)을 가진 곳이 parentRegion
                int maxLength = 0;
                int parentRegionId = -1;

                for (RegionName parentRegion : parentRegions) {
                    int currentLength = parentRegion.getName().length();

                    if (currentLength <= maxLength) continue;

                    maxLength = currentLength;
                    parentRegionId = parentRegion.getLegalCodeId();
                }

                regionName.setParentId(parentRegionId);
                regionData.add(regionName);
            }
            realEstateUploadDao.uploadRegionNames(regionData);
        }
    }
}
