package br.com.caelum.leilao.servico;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.mockito.InOrder;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;

public class EncerradorDeLeilaoTest {

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAntes() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("CEL").naData(antiga).constroi();

		List<Leilao> encerrados = Arrays.asList(leilao1, leilao2);

		RepositorioDeLeiloes leilaoDaoFalso = mock(RepositorioDeLeiloes.class);
		when(leilaoDaoFalso.correntes()).thenReturn(encerrados);
		
		EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(leilaoDaoFalso, carteiroFalso);
		encerrador.encerra();

		assertEquals(encerrador.getTotalEncerrados(), 2);
		assertTrue(encerrados.get(0).isEncerrado());
		assertTrue(encerrados.get(1).isEncerrado());
	}

	@Test
	public void naoDeveEncerrarLeiloesQueComecaramOntem() {
		Calendar antiga = Calendar.getInstance();
		antiga.add(Calendar.DAY_OF_MONTH, -1);

		Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("CEL").naData(antiga).constroi();

		List<Leilao> encerrados = Arrays.asList(leilao1, leilao2);

		LeilaoDao leilaoDaoFalso = mock(LeilaoDao.class);
		when(leilaoDaoFalso.correntes()).thenReturn(encerrados);
		
		EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
		
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(leilaoDaoFalso, carteiroFalso);
		encerrador.encerra();

		assertEquals(encerrador.getTotalEncerrados(), 0);
		assertFalse(encerrados.get(0).isEncerrado());
		assertFalse(encerrados.get(1).isEncerrado());
	}

	@Test
	public void naoTemLeiloesParaEncerrar() {
		LeilaoDao leilaoDaoFalso = mock(LeilaoDao.class);
		when(leilaoDaoFalso.correntes()).thenReturn(new ArrayList<Leilao>());
		
		EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(leilaoDaoFalso, carteiroFalso);
		encerrador.encerra();

		assertEquals(encerrador.getTotalEncerrados(), 0);
	}

	// @Test
	// public void testMock() {
	// LeilaoDao leilaoDaoFalso = mock(LeilaoDao.class);
	// when(leilaoDaoFalso.teste()).thenReturn("xxx");
	//
	// assertEquals("xxx", leilaoDaoFalso.teste());
	// }

	@Test
	public void deveAtualizarLeiloesEncerrados() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);
		
		Leilao leilao = new CriadorDeLeilao().para("tv").naData(antiga).constroi();
		
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao));

		EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();
		
		verify(daoFalso, times(1)).atualiza(leilao);
		
		  // passamos os mocks que serao verificados
        InOrder inOrder = inOrder(carteiroFalso, daoFalso);
        
        // a primeira invocação
        inOrder.verify(daoFalso, times(1)).atualiza(leilao);    
        
     // a segunda invocação
        inOrder.verify(carteiroFalso, times(1)).envia(leilao);
	}
	
	@Test
    public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {

        Calendar ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -1);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
            .naData(ontem).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
            .naData(ontem).constroi();

        RepositorioDeLeiloes daoFalso = mock(LeilaoDao.class);
        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilao1.isEncerrado());
        assertFalse(leilao2.isEncerrado());

        verify(daoFalso, never()).atualiza(leilao1);
        verify(daoFalso, never()).atualiza(leilao2);
        
        verify(daoFalso, atLeastOnce()).correntes();
        verify(daoFalso, atLeast(1)).correntes();
        verify(daoFalso, atMost(1)).correntes();
    }
	
	@Test
	public void naoDeveContinuarCasoDaoLanceExcecao(){
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999,  1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Radio").naData(antiga).constroi();
		
		EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();
		
		verify(daoFalso, times(1)).atualiza(leilao2);
		verify(carteiroFalso).envia(leilao2);
		
		verify(carteiroFalso, never()).envia(leilao1);
	}
	
	@Test
	public void naoDeveEnviarNenhumEmail(){
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999,  1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Radio").naData(antiga).constroi();
		
		EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		doThrow(new RuntimeException()).when(daoFalso).atualiza(any(Leilao.class));
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();
		
		verify(carteiroFalso, never()).envia(any(Leilao.class));
	}
}
