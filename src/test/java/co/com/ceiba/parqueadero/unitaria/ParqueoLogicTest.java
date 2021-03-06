package co.com.ceiba.parqueadero.unitaria;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import co.com.ceiba.parqueadero.dominio.Carro;
import co.com.ceiba.parqueadero.dominio.Moto;
import co.com.ceiba.parqueadero.dominio.Parqueo;
import co.com.ceiba.parqueadero.dominio.Vehiculo;
import co.com.ceiba.parqueadero.exception.ParqueaderoException;
import co.com.ceiba.parqueadero.facade.ParqueoFacadeInterface;
import co.com.ceiba.parqueadero.interfaces.IParqueo;
import co.com.ceiba.parqueadero.logic.ParqueoLogic;
import co.com.ceiba.parqueadero.repositorio.RespositorioParqueo;
import co.com.ceiba.parqueadero.testdatabuilder.VehiculoTestDataBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.DEFINED_PORT)
public class ParqueoLogicTest {
	
	private static final String CON_RESTRICCIONES="Tiene restricciones: No es un dia habil para su vehiculo";
	private static final String CUPO_NO_DISPONIBLE="Cupo no disponible";
	private static final double DELTA=0.01;
	private static final int LUNES=1;
	private static final int MIERCOLES=3;
	private static final int DOMINGO=7;
	
	@Autowired
	private IParqueo parqueoInterface;
	
	@Mock
	private ParqueoFacadeInterface parqueoFacadeInterface;
		
	private ParqueoLogic parqueoLogic;
	
	@Autowired
	private RespositorioParqueo repositorioParqueo;
	
	@Before
	public void inicializacion(){
		parqueoLogic=new ParqueoLogic(parqueoFacadeInterface);
		repositorioParqueo.deleteAll();
	}
	
	@After
	public void finalizacion(){
		repositorioParqueo.deleteAll();
	}
	
	@Test
	public void sinRestriccionesTest(){
		//Arrange
			Vehiculo v=new VehiculoTestDataBuilder().buildCarro();
		//Act
			boolean flag=parqueoInterface.sinRestricciones(v, LUNES);
		//Assert
			assertTrue(flag);		
	}
	
	@Test
	public void sinRestriccionesTestException(){
		//Arrange
			Vehiculo v=new VehiculoTestDataBuilder().conPlaca("AAA-123").buildCarro();			
		//Act
			try{
				parqueoInterface.sinRestricciones(v, MIERCOLES);
				fail();
			}
			catch(ParqueaderoException e){
		//Assert
				assertEquals(CON_RESTRICCIONES,e.getMessage());
			}
	}
	
	@Test
	public void sinRestriccionesTestNull(){
		//Arrange
			Vehiculo v=null;			
		//Act
			try{
				parqueoInterface.sinRestricciones(v, MIERCOLES);
				fail();
			}
			catch(ParqueaderoException e){
		//Assert
				assertEquals(CON_RESTRICCIONES,e.getMessage());
			}
	}
	
	@Test
	public void sinRestriccionesTestVehiculoConPermiso(){
		//Arrange
			Vehiculo v=new VehiculoTestDataBuilder().conPlaca("BBB-123").buildCarro();		
		//Act			
			boolean flag=parqueoInterface.sinRestricciones(v, LUNES);						
		//Assert
			assertTrue(flag);				
	}
	
	@Test
	public void disponibleParaMotosTest(){
		//Arrange
			List<Parqueo> listaParqueo=new ArrayList<>(); 
			int numPlaca=100;
			for(int i=0;i<9;i++){
				listaParqueo.add(new Parqueo(new DateTime(), new DateTime(), 5000, new Moto("BBC-"+numPlaca, 500)));
				numPlaca++;
			}			
			Mockito.when(parqueoFacadeInterface.celdasOcupadas()).thenReturn(listaParqueo);
			Vehiculo v=new VehiculoTestDataBuilder().buildMoto();			
		//Act			
			boolean flag=parqueoLogic.disponible(v);			
		//Assert
			assertTrue(flag);			
	}
	
	@Test
	public void disponibleParaMotos2Test(){
		//Arrange
			List<Parqueo> listaParqueo=new ArrayList<>(); 
			int numPlaca=100;
			for(int i=0;i<10;i++){
				listaParqueo.add(new Parqueo(new DateTime(), new DateTime(), 5000, new Moto("BBC-"+numPlaca, 500)));
				numPlaca++;
			}			
			Mockito.when(parqueoFacadeInterface.celdasOcupadas()).thenReturn(listaParqueo);
			Vehiculo v=new VehiculoTestDataBuilder().buildMoto();			
		//Act	
			try{
				parqueoLogic.disponible(v);
				fail();
			}
			catch(ParqueaderoException e){
		//Assert
				assertEquals(CUPO_NO_DISPONIBLE, e.getMessage());
			}
	}
	
	@Test
	public void disponibleParaCarrosTest(){
		//Arrange			
			List<Parqueo> listaParqueo = listaDeCarrosParqueados(15);
			Mockito.when(parqueoFacadeInterface.celdasOcupadas()).thenReturn(listaParqueo);
			Vehiculo v=new VehiculoTestDataBuilder().buildCarro();			
		//Act			
			boolean flag=parqueoLogic.disponible(v);			
		//Assert
			assertTrue(flag);			
	}
	
	@Test
	public void disponibleParaCarroExceptionTest(){
		//Arrange
			List<Parqueo> listaParqueo = listaDeCarrosParqueados(22);
			Mockito.when(parqueoFacadeInterface.celdasOcupadas()).thenReturn(listaParqueo);
			Vehiculo v=new VehiculoTestDataBuilder().buildCarro();			
		//Act
			try{
				parqueoLogic.disponible(v);
				fail();
			}
			catch(ParqueaderoException e){
		//Assert
				assertEquals(CUPO_NO_DISPONIBLE,e.getMessage());			
			}
	}

	private List<Parqueo> listaDeCarrosParqueados(int cantidadDeCarros) {
		List<Parqueo> listaParqueo=new ArrayList<>(); 
		int numPlaca=100;
		for(int i=0;i<cantidadDeCarros;i++){
			listaParqueo.add(new Parqueo(new DateTime(), new DateTime(), 5000, new Carro("BBC-"+numPlaca, 500)));
			numPlaca++;
		}
		return listaParqueo;
	}
	
	@Test
	public void disponibleParaMotoExceptionTest(){
		//Arrange
			List<Parqueo> listaParqueo=new ArrayList<>(); 
			int numPlaca=100;
			for(int i=0;i<19;i++){
				listaParqueo.add(new Parqueo(new DateTime(), new DateTime(), 5000, new Moto("BBC-"+numPlaca, 500)));
				numPlaca++;
			}			
			Mockito.when(parqueoFacadeInterface.celdasOcupadas()).thenReturn(listaParqueo);
			Vehiculo v=new VehiculoTestDataBuilder().buildMoto();			
		//Act
			try{
				parqueoLogic.disponible(v);
				fail();
			}
			catch(ParqueaderoException e){
		//Assert
				assertEquals(CUPO_NO_DISPONIBLE,e.getMessage());			
			}
	}
	
	/**
	 * Valor a pagar de una moto(CILINDRAJE=650) en 10 horas: debe ser 6.000
	 */
	@Test
	public void calcularValorPagarMotoTest(){
		//Arrange
			DateTime fechaIngreso=new DateTime();
			DateTime fechaSalida=new DateTime().plusHours(10);
			Vehiculo v=new VehiculoTestDataBuilder().conCilindraje(650).buildMoto();
			double valorEsperado=6000;
		//Act			
			double valorPagar=parqueoLogic.calcularValorPagar(fechaIngreso, fechaSalida, v);			
		//Assert
			assertEquals(valorPagar,valorEsperado,DELTA);
	}
	
	/**
	 * Valor a pagar de una moto(CILINDRAJE=500) en 9 horas: debe ser 4000
	 */
	@Test
	public void calcularValorPagarMotoNueveHorasTest(){
		//Arrange
			DateTime fechaIngreso=new DateTime();
			DateTime fechaSalida=new DateTime().plusHours(9);
			Vehiculo v=new VehiculoTestDataBuilder().conCilindraje(500).buildMoto();
			double valorEsperado=4000;
		//Act			
			double valorPagar=parqueoLogic.calcularValorPagar(fechaIngreso, fechaSalida, v);			
		//Assert
			assertEquals(valorPagar,valorEsperado,DELTA);
	}
	
	/**
	 * Valor a pagar de un carro en 3 horas: debe ser 3000
	 */
	@Test
	public void calcularValorPagarCarroTest(){
		//Arrange
			DateTime fechaIngreso=new DateTime();
			DateTime fechaSalida=new DateTime().plusHours(3);
			Vehiculo v=new VehiculoTestDataBuilder().buildCarro();
			double valorEsperado=3000;
		//Act			
			double valorPagar=parqueoLogic.calcularValorPagar(fechaIngreso, fechaSalida, v);
		//Assert
			assertEquals(valorPagar,valorEsperado,DELTA);
	}
	
	/**
	 * Valor a pagar de un carro en 9 horas: debe ser 8000
	 */
	@Test
	public void calcularValorPagarCarroNueveHoarasTest(){
		//Arrange
			DateTime fechaIngreso=new DateTime();
			DateTime fechaSalida=new DateTime().plusHours(9);
			Vehiculo v=new VehiculoTestDataBuilder().buildCarro();
			double valorEsperado=8000;
		//Act			
			double valorPagar=parqueoLogic.calcularValorPagar(fechaIngreso, fechaSalida, v);
		//Assert
			assertEquals(valorPagar,valorEsperado,DELTA);
	}
	
	/**
	 * Valor a pagar de una moto(CILINDRAJE=250) en 1 dia y 5 horas: debe ser 6500
	 */
	@Test
	public void calcularValorPagarMoto200Test(){
		//Arrange
			DateTime fechaIngreso=new DateTime();
			DateTime fechaSalida=new DateTime().plusDays(1).plusHours(5);
			Vehiculo v=new VehiculoTestDataBuilder().buildMoto();
			double valorEsperado=6500;
		//Act			
			double valorPagar=parqueoLogic.calcularValorPagar(fechaIngreso, fechaSalida, v);			
		//Assert
			assertEquals(valorPagar,valorEsperado,DELTA);
	}
	
	/**
	 * Valor a pagar de una moto(CILINDRAJE=501) en 1 dia y 8 horas y 49 minutos: debe ser 10500
	 */
	@Test
	public void calcularValorPagarMoto501Test(){
		//Arrange
			DateTime fechaIngreso=new DateTime();
			DateTime fechaSalida=new DateTime().plusDays(1).plusHours(8).plusMinutes(49);
			Vehiculo v=new VehiculoTestDataBuilder().conCilindraje(501).buildMoto();
			double valorEsperado=10500;
		//Act			
			double valorPagar=parqueoLogic.calcularValorPagar(fechaIngreso, fechaSalida, v);			
		//Assert			
			assertEquals(valorPagar,valorEsperado,DELTA);
	}
	
	/**
	 * Valor a pagar de una moto(CILINDRAJE=300) en 1 dia y 9 horas 1 y  minutos: debe ser 8000
	 */
	@Test
	public void calcularValorPagarMoto300Test(){
		//Arrange
			DateTime fechaIngreso=new DateTime();
			DateTime fechaSalida=new DateTime().plusDays(1).plusHours(9).plusMinutes(1);
			Vehiculo v=new VehiculoTestDataBuilder().conCilindraje(300).buildMoto();
			double valorEsperado=8000;
		//Act			
			double valorPagar=parqueoLogic.calcularValorPagar(fechaIngreso, fechaSalida, v);			
		//Assert			
			assertEquals(valorPagar,valorEsperado,DELTA);
	}
	
	/**
	 * Valor a pagar de una moto(CILINDRAJE=600) en 2 minutos : debe ser 2500
	 */
	@Test
	public void calcularValorPagarMoto600Test(){
		//Arrange
			DateTime fechaIngreso=new DateTime();
			DateTime fechaSalida=new DateTime(fechaIngreso).plusMinutes(2);			
			Vehiculo v=new VehiculoTestDataBuilder().conCilindraje(600).buildMoto();
			double valorEsperado=2500;
		//Act			
			double valorPagar=parqueoLogic.calcularValorPagar(fechaIngreso, fechaSalida, v);			
		//Assert			
			assertEquals(valorPagar,valorEsperado,DELTA);
	}
	
	/**
	 * Valor a pagar de una moto en 50 segundos : debe ser 0
	 */
	@Test
	public void calcularValorPagarMotoSegundosTest(){
		//Arrange
			DateTime fechaIngreso=new DateTime();
			DateTime fechaSalida=new DateTime(fechaIngreso).plusSeconds(50);			
			Vehiculo v=new VehiculoTestDataBuilder().buildMoto();
			double valorEsperado=0;
		//Act			
			double valorPagar=parqueoLogic.calcularValorPagar(fechaIngreso, fechaSalida, v);
			System.out.println("valor a pagar="+valorPagar);
		//Assert			
			assertEquals(valorPagar,valorEsperado,DELTA);
	}
	
	/**
	 * Valor a pagar de una carro en 1 dia y 3 horas: debe ser 11000
	 */
	@Test
	public void calcularValorPagarCarroUnDiaTest(){
		//Arrange
			DateTime fechaIngreso=new DateTime();
			DateTime fechaSalida=new DateTime().plusDays(1).plusHours(3);
			Vehiculo v=new VehiculoTestDataBuilder().buildCarro();
			double valorEsperado=11000;
		//Act			
			double valorPagar=parqueoLogic.calcularValorPagar(fechaIngreso, fechaSalida, v);
		//Assert
			assertEquals(valorPagar,valorEsperado,DELTA);
	}
	
	/**
	 * Valor a pagar de una carro en 2 minutos debe ser 1000
	 */
	@Test
	public void calcularValorPagarCarroDosMinutosTest(){
		//Arrange
			DateTime fechaIngreso=new DateTime();
			DateTime fechaSalida=new DateTime().plusMinutes(2);
			Vehiculo v=new VehiculoTestDataBuilder().buildCarro();
			double valorEsperado=1000;
		//Act			
			double valorPagar=parqueoLogic.calcularValorPagar(fechaIngreso, fechaSalida, v);
		//Assert
			assertEquals(valorPagar,valorEsperado,DELTA);
	}
	
}
