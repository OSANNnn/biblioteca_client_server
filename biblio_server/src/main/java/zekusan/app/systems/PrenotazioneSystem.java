package zekusan.app.systems;

import java.io.IOException;
import java.util.List;

import zekusan.models.comms.Status;
import zekusan.models.comms.requests.PrenotazioneRequest;
import zekusan.models.comms.responses.PrenotazioneResponse;
import zekusan.models.items.Item;

public class PrenotazioneSystem {
	private PrenotazioneSystem() {
		
	}
	
	public static PrenotazioneResponse respond(PrenotazioneRequest request) {
		PrenotazioneResponse response = new PrenotazioneResponse();	
		response.setItemId(request.getItemId());
		
		try {
			List<Item> catalogo = CatalogoGen.getLista(request.getType());
			boolean found = false;
			for(Item i : catalogo) {
				if (request.getItemId() == i.getId()) {
					found = true;
					if (i.getQuantita() > 0) {
						i.setQuantita(i.getQuantita() - 1);
						CatalogoGen.updateCatalog(catalogo, request.getType());
						response.setStatus(Status.SUCCESS);
					}
					else
					{
						response.setStatus(Status.BAD_REQUEST);
					}
					break;
				}
			}
			if (!found) {
				response.setStatus(Status.NOT_FOUND);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			response.setStatus(Status.BAD_REQUEST);
		}
		
		
		return response;
	}
}
