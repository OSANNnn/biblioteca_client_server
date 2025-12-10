package zekusan.app.systems;

import java.util.List;

import zekusan.comms.requests.CatalogoRequest;
import zekusan.comms.responses.CatalogoResponse;
import zekusan.enums.Status;
import zekusan.models.items.Item;

public class CatalogSystem {
	private CatalogSystem() {
		
	}
	
	public static CatalogoResponse respond(CatalogoRequest request) {
		CatalogoResponse response = new CatalogoResponse();

		response.setCategoria(request.getCategoria());
		setList(response);
		response.setStatus(Status.SUCCESS);
		
		return response;
	}

	private static void setList(CatalogoResponse response) {
		List<Item> lista = null;
		try {
			lista = CatalogoGen.getLista(response.getCategoria());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		response.setCatalogo(lista);
	}
}
