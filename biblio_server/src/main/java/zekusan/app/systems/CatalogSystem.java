package zekusan.app.systems;

import java.util.ArrayList;

import zekusan.models.comms.Status;
import zekusan.models.comms.requests.CatalogoRequest;
import zekusan.models.comms.responses.CatalogoResponse;
import zekusan.models.items.Item;

public class CatalogSystem {
	public static CatalogoResponse respond(CatalogoRequest request) {
		CatalogoResponse response = new CatalogoResponse();

		response.setCategoria(request.getCategoria());
		setList(response);
		response.setStatus(Status.SUCCESS);
		
		return response;
	}

	private static void setList(CatalogoResponse response) {
		ArrayList<Item> lista = null;
		try {
			lista = CatalogoGen.getLista(response.getCategoria());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		response.setCatalogo(lista);
	}
}
