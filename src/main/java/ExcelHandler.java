import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ExcelHandler {
    public static ArrayList<Card> getCardsFromExcel(String path, Integer sheetIndex) {
        ArrayList<Card> ret = new ArrayList<>();
        XSSFWorkbook xssfWorkbook = null;
        try {
            xssfWorkbook = new XSSFWorkbook(new FileInputStream(path));
            XSSFSheet sheet = xssfWorkbook.getSheetAt(sheetIndex);
            int maxRow = sheet.getLastRowNum();
            for (int row = 1; row <= maxRow; row++) {
                String cardStr = sheet.getRow(row).getCell(0).toString();
                Card card = Card.parse(cardStr);
                if (card.color != Card.Color.ADV && card.color != Card.Color.BIG_G && card.color != Card.Color.LITTLE_G){
                    if (ret.contains(card)){
                        assert false;
                    }
                }
                ret.add(card);
//                System.out.print(card + "  ");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public static void WriteCardsToExcel(ArrayList<Card> cards){
        XSSFWorkbook xssfWorkbook = null;
        String path;
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            // 苹果
            path = "src/BerglasOptimized.xlsx";
        } else {
            // windows
            path = "src\\BerglasOptimized.xlsx";
        }
        File yourFile = new File(path);
        try {
            yourFile.createNewFile(); // if file already exists will do nothing
            if (yourFile.length() != 0){
                xssfWorkbook = new XSSFWorkbook(new FileInputStream(path));
            }else{
                xssfWorkbook = new XSSFWorkbook();
            }
            XSSFSheet sheet = xssfWorkbook.createSheet();

            for (int i = 0; i < cards.size(); i++) {
                sheet.createRow(i+1).createCell(0).setCellValue(cards.get(i).toString());
            }
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            xssfWorkbook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}





