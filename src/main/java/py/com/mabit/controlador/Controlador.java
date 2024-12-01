package py.com.mabit.controlador;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Maria R. Garcete
 * @author Gabriel González Rojas
 * MAESTRIA EN INFORMATICA - FPUNE
 */
@RestController
public class Controlador {
	@GetMapping("/top-peliculas-tarea6-chrome")
	public List<Pelicula> obtenerListaPeliculas() {
		List<Pelicula> lista = new ArrayList<>();	
		File archivoDriver = new File(new File("src/main/resources/driver/chromedriver.exe").getAbsolutePath());
		System.out.println(archivoDriver);
		System.setProperty("webdriver.chrome.driver", archivoDriver.toString());
		ChromeOptions options = new ChromeOptions();
		//options.addArguments("--headless"); // Ejecutar Chrome sin interfaz gráfica
		WebDriver driver = new ChromeDriver(options);
		try {
			driver.get("https://www.imdb.com/search/title/?release_date=2023-01-01,2023-12-31&sort=boxoffice_gross_us,desc");
			WebDriverWait esperar = new WebDriverWait(driver, Duration.ofSeconds(15));
			esperar.until(ExpectedConditions.presenceOfElementLocated(By.id("__NEXT_DATA__")));

			WebElement raizFuente = driver.findElement(By.id("__NEXT_DATA__"));
			String json = raizFuente.getAttribute("innerHTML");

			JSONObject objetoJSON = new JSONObject(json);
			JSONObject resultadosJSON = objetoJSON.getJSONObject("props").getJSONObject("pageProps")
					.getJSONObject("searchResults").getJSONObject("titleResults");

			JSONArray itemTitulos = resultadosJSON.getJSONArray("titleListItems");

			// Recorrer las películas y extraer la información
			for (int i = 0; i < itemTitulos.length(); i++) {
				JSONObject pelicula = itemTitulos.getJSONObject(i);

				String nombre = pelicula.getString("titleText");
				Double recaudacion = pelicula.getJSONObject("ratingSummary").getDouble("voteCount");
				Double puntaje = pelicula.getJSONObject("ratingSummary").getDouble("aggregateRating");

				// Verificar que no esté vacío
				if (!nombre.isEmpty() && recaudacion != null && puntaje != null) {
					lista.add(new Pelicula(nombre, recaudacion, puntaje));
				}

				// Limitar a 40 películas
				if (lista.size() == 40) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}
		List<Pelicula> peliculasOrdenadas = lista.stream()
	            .sorted(Comparator.comparing(Pelicula::getRecaudacion).reversed())
	            .collect(Collectors.toList());
		return peliculasOrdenadas;
	}

	public static class Pelicula {
		private String nombre;
		private Double recaudacion;
		private Double puntaje;

		public Pelicula(String nombre, Double recaudacion, Double puntaje) {
			super();
			this.nombre = nombre;
			this.recaudacion = recaudacion;
			this.puntaje = puntaje;
		}

		public String getNombre() {
			return nombre;
		}

		public void setNombre(String nombre) {
			this.nombre = nombre;
		}

		public Double getRecaudacion() {
			return recaudacion;
		}

		public void setRecaudacion(Double recaudacion) {
			this.recaudacion = recaudacion;
		}

		public Double getPuntaje() {
			return puntaje;
		}

		public void setPuntaje(Double puntaje) {
			this.puntaje = puntaje;
		}
	}
}
