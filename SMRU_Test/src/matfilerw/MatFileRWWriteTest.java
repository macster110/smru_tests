package matfilerw;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;
import com.jmatio.types.MLStructureObjectBase;

import java.awt.datatransfer.*;
import java.awt.Toolkit;

public class MatFileRWWriteTest {
	
	
	class FileTransferable implements Transferable {
		
	    private final File file;

	    public FileTransferable(File file) {
	        this.file = file;
	    }

	    @Override
	    public DataFlavor[] getTransferDataFlavors() {
	        return new DataFlavor[] { DataFlavor.javaFileListFlavor };
	    }

	    @Override
	    public boolean isDataFlavorSupported(DataFlavor flavor) {
	        return DataFlavor.javaFileListFlavor.equals(flavor);
	    }

	    @Override
	    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
	        final ArrayList<File> files = new ArrayList<File>();
	        files.add(file);
	        return files;
	    }
	}
	
	
		
	public static void main(String[] args) { 
		
		System.out.println("Hello MATLAB 1");
		File file=new File("C:\\Users\\macst\\Desktop\\hello.mat"); 
		
		// test column-packed vector
		double[] src = new double[]{1.3, 2.0, 3.0, 4.0, 5.0, 6.0};

		// create 3x2 double matrix
		// [ 1.0 4.0 ;
		// 2.0 5.0 ;
		// 3.0 6.0 ]
		MLDouble mlDouble = new MLDouble(null, src, 3);
		MLChar mlChar = new MLChar(null, "I am dummy");

		
		System.out.println("Hello MATLAB 2");
		MLStructure mlStruct = new MLStructure("str", new int[]{2, 1});
		mlStruct.setField("f1", mlDouble,1);
		mlStruct.setField("f2", mlChar,1);
		
		
		System.out.println("Hello MATLAB 3");
		mlStruct.setField("f1", mlDouble,2);
		mlStruct.setField("f2", mlChar,2);
				

		

		// write array to file
		ArrayList<MLArray> list = new ArrayList<MLArray>();
		list.add(mlStruct);

		System.out.println("Hello MATLAB 4");
		// write arrays to file
		try {
			MatFileWriter filewrite=new MatFileWriter(file, list);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// read array form file
//		MatFileReader mfr = new MatFileReader(file);
//		MLStructure mlArrayRetrived = (MLStructure) mfr.getMLArray("str");

//		assertEquals(mlDouble, mlArrayRetrived.getField("f1"));
//		assertEquals(mlChar, mlArrayRetrived.getField("f2"));
		
		
//		
//		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
//		import java.awt.datatransfer.
//		clpbrd.setContents(stringSelection, null);
//		

		
	}


}
