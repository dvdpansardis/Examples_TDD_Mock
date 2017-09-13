package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.dominio.Leilao;

public class ServicoDeEmail implements EnviadorDeEmail {

	public void envia(Leilao leilao) {
		
		System.out.println("Enviando email para " + leilao.getDescricao());
		
	}

}
