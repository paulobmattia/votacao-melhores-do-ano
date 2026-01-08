import express from 'express';
import { createServer } from 'http';
import { WebSocketServer } from 'ws';
import { v4 as uuidv4 } from 'uuid';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express();
const server = createServer(app);
const wss = new WebSocketServer({ server });

// =============================================================================
// ESTADO GLOBAL (Single Source of Truth)
// =============================================================================

const gameState = {
    fase: 'aguardando', // aguardando | votacao-aberta | votacao-fechada | revelacao
    indiceCategoriaAtual: 0,
    categorias: [
        {
            id: 1,
            nome: '📻 Rádio Patrulha',
            descricao: 'Aquela pessoa que sempre nos mantém informados, seja do que acontece na nossa cidade, ou na vida dos nossos amigos. O irmão Bomba Patch - 100% atualizado',
            candidatos: [
                { id: 101, nome: 'Diego C.' },
                { id: 102, nome: 'Rayssa' },
                { id: 103, nome: 'Dudu' },
                { id: 104, nome: 'Rebequinha' }
            ]
        },
        {
            id: 2,
            nome: '🚗 Frete Humano',
            descricao: 'Precisa de carona? É a pessoa que você sabe que pode contar!!',
            candidatos: [
                { id: 201, nome: 'Jefferson' },
                { id: 202, nome: 'Dudu' },
                { id: 203, nome: 'Victor' }
            ]
        },
        {
            id: 3,
            nome: '📖 Melhor Professor de EBD',
            descricao: 'Aquele professor que dá uma aula caprichada',
            candidatos: [
                { id: 301, nome: 'Paulo' },
                { id: 302, nome: 'Cyntia' },
                { id: 303, nome: 'Bia' }
            ]
        },
        {
            id: 4,
            nome: '⚽ Torcedor Mais Fanático',
            descricao: 'Não aguenta mais ouvir seu amigo falando de futebol? Não aguenta mais ficar lendo os textos de Edésio?? É a sua hora de escolher o mais fanático',
            candidatos: [
                { id: 401, nome: 'Edésio' },
                { id: 402, nome: 'Dudu' },
                { id: 403, nome: 'Estevo' },
                { id: 404, nome: 'Diego G.' },
                { id: 405, nome: 'Nathan' }
            ]
        },
        {
            id: 5,
            nome: '🎸 One Man Band',
            descricao: 'Uma semana ele tá na bateria, outra na guitarra, outra no violão, daqui a pouco vai parar na mesa de som',
            candidatos: [
                { id: 501, nome: 'Théo' },
                { id: 502, nome: 'Victor' },
                { id: 503, nome: 'Esdras' }
            ]
        },
        {
            id: 6,
            nome: '🎯 Joga nas 11',
            descricao: 'Você não sabe se vai encontrar ele na cantina, na mesa de som, no ministério infantil…',
            candidatos: [
                { id: 601, nome: 'Théo' },
                { id: 602, nome: 'Rafael' },
                { id: 603, nome: 'Diego C.' },
                { id: 604, nome: 'Dudu' }
            ]
        },
        {
            id: 7,
            nome: '🎤 Golden Voice',
            descricao: 'A voz... de ouro.',
            candidatos: [
                { id: 701, nome: 'Damylle' },
                { id: 702, nome: 'Priscilla' },
                { id: 703, nome: 'Diego G.' },
                { id: 704, nome: 'Anna Clara' }
            ]
        },
        {
            id: 8,
            nome: '🖼️ Sticker Maker',
            descricao: 'A pessoa que faz as melhores figurinhas e montagens da galera',
            candidatos: [
                { id: 801, nome: 'Paulo' },
                { id: 802, nome: 'Théo' }
            ]
        },
        {
            id: 9,
            nome: '📱 Blogueirin',
            descricao: 'O influencer da galera',
            candidatos: [
                { id: 901, nome: 'Maria' },
                { id: 902, nome: 'Jefferson' },
                { id: 903, nome: 'Kaianne' }
            ]
        },
        {
            id: 10,
            nome: '🤝 Miss/Mr Simpatia',
            descricao: 'Você sabe que não vai sair do culto sem essa pessoa falar com você',
            candidatos: [
                { id: 1001, nome: 'Maria' },
                { id: 1002, nome: 'Edésio' },
                { id: 1003, nome: 'Nathan' },
                { id: 1004, nome: 'Rayssa' }
            ]
        },
        {
            id: 11,
            nome: '👑 Rei/Rainha da Zoeira',
            descricao: 'The zoeira never ends',
            candidatos: [
                { id: 1101, nome: 'Edésio' },
                { id: 1102, nome: 'Paulo' }
            ]
        },
        {
            id: 12,
            nome: '🏎️ Rubens Barrichello',
            descricao: 'Aquela pessoa que você precisa marcar meia hora mais cedo',
            candidatos: [
                { id: 1201, nome: 'Victor' },
                { id: 1202, nome: 'Esdras' },
                { id: 1203, nome: 'Débora' },
                { id: 1204, nome: 'Kaianne' },
                { id: 1205, nome: 'Estevo' }
            ]
        },
        {
            id: 13,
            nome: '🙊 Without Notion™',
            descricao: 'Esse irmão precisa de um pouco mais de tato',
            candidatos: [
                { id: 1301, nome: 'Dudu' },
                { id: 1302, nome: 'Estevo' },
                { id: 1303, nome: 'Luma' },
                { id: 1304, nome: 'Maria' }
            ]
        },
        {
            id: 14,
            nome: '😴 Rivotril',
            descricao: 'Prêmio para esse irmão que está agora olhando pro nada com a cabeça voando...',
            candidatos: [
                { id: 1401, nome: 'Esdras' },
                { id: 1402, nome: 'Estela' },
                { id: 1403, nome: 'Victor' },
                { id: 1404, nome: 'Ylbert' },
                { id: 1405, nome: 'Maria' }
            ]
        },
        {
            id: 15,
            nome: '⭐ Revelação',
            descricao: 'Irmão que despontou esse ano com algum talento até então desconhecido',
            candidatos: [
                { id: 1501, nome: 'Anna Clara (Louvor)' },
                { id: 1502, nome: 'Luma (Bateria)' },
                { id: 1503, nome: 'Rafael (Baixo)' }
            ]
        },
        {
            id: 16,
            nome: '🗣️ Mais Tagarela',
            descricao: 'Fala demaaaais, Tite!',
            candidatos: [
                { id: 1601, nome: 'Nathan' },
                { id: 1602, nome: 'Estela' },
                { id: 1603, nome: 'Rayssa' },
                { id: 1604, nome: 'Maria' }
            ]
        },
        {
            id: 17,
            nome: '🔥 Mais Competitivo',
            descricao: 'A pessoa que vai com sangue nos olhos, que não passa a bola, que te olha e te chama de "safada!!" em um jogo de cartas',
            candidatos: [
                { id: 1701, nome: 'Paulo' },
                { id: 1702, nome: 'Maria' },
                { id: 1703, nome: 'Théo' },
                { id: 1704, nome: 'Nathan' },
                { id: 1705, nome: 'Luma' },
                { id: 1706, nome: 'Jefferson' }
            ]
        }
    ],
    votos: {}, // { idCategoria: { idCandidato: count } }
    sessoesVotaram: new Set() // IDs que votaram na categoria atual
};

// =============================================================================
// FUNÇÕES DE ESTADO
// =============================================================================

function getCategoriaAtual() {
    return gameState.categorias[gameState.indiceCategoriaAtual];
}

function getIdCategoriaAtual() {
    return getCategoriaAtual()?.id;
}

function usuarioJaVotou(sessionId) {
    return gameState.sessoesVotaram.has(sessionId);
}

function getVotosCategoria(idCategoria) {
    return gameState.votos[idCategoria] || {};
}

function getTotalVotosCategoria(idCategoria) {
    const votos = getVotosCategoria(idCategoria);
    return Object.values(votos).reduce((a, b) => a + b, 0);
}

function getVencedorCategoria(idCategoria) {
    const votos = getVotosCategoria(idCategoria);
    const categoria = gameState.categorias.find(c => c.id === idCategoria);

    if (!categoria || Object.keys(votos).length === 0) return null;

    const idVencedor = Object.entries(votos).reduce((a, b) => (b[1] > a[1] ? b : a))[0];
    return categoria.candidatos.find(c => c.id === parseInt(idVencedor));
}

// =============================================================================
// AÇÕES DE TRANSIÇÃO
// =============================================================================

function votar(sessionId, idCandidato) {
    const idCat = getIdCategoriaAtual();

    if (gameState.fase !== 'votacao-aberta' || usuarioJaVotou(sessionId)) {
        return false;
    }

    if (!gameState.votos[idCat]) {
        gameState.votos[idCat] = {};
    }

    if (!gameState.votos[idCat][idCandidato]) {
        gameState.votos[idCat][idCandidato] = 0;
    }

    gameState.votos[idCat][idCandidato]++;
    gameState.sessoesVotaram.add(sessionId);

    return true;
}

function abrirVotacao() {
    if (gameState.fase === 'aguardando') {
        gameState.fase = 'votacao-aberta';
        return true;
    }
    return false;
}

function fecharVotacao() {
    if (gameState.fase === 'votacao-aberta') {
        gameState.fase = 'votacao-fechada';
        return true;
    }
    return false;
}

function revelarVencedor() {
    if (gameState.fase === 'votacao-fechada') {
        gameState.fase = 'revelacao';
        return true;
    }
    return false;
}

function proximaCategoria() {
    const proximoIndice = gameState.indiceCategoriaAtual + 1;
    if (proximoIndice < gameState.categorias.length) {
        gameState.indiceCategoriaAtual = proximoIndice;
        gameState.fase = 'aguardando';
        gameState.sessoesVotaram.clear();
        return true;
    }
    return false;
}

function resetarCategoria() {
    const idCat = getIdCategoriaAtual();
    gameState.fase = 'aguardando';
    gameState.votos[idCat] = {};
    gameState.sessoesVotaram.clear();
    return true;
}

// =============================================================================
// WEBSOCKET - Sincronização em Tempo Real
// =============================================================================

function getPublicState(sessionId) {
    const categoria = getCategoriaAtual();
    const idCat = getIdCategoriaAtual();

    return {
        fase: gameState.fase,
        indiceCategoriaAtual: gameState.indiceCategoriaAtual,
        totalCategorias: gameState.categorias.length,
        categoria: categoria,
        jaVotou: usuarioJaVotou(sessionId),
        vencedor: gameState.fase === 'revelacao' ? getVencedorCategoria(idCat) : null
    };
}

function getAdminState(sessionId) {
    const publicState = getPublicState(sessionId);
    const idCat = getIdCategoriaAtual();

    return {
        ...publicState,
        votos: getVotosCategoria(idCat),
        totalVotos: getTotalVotosCategoria(idCat)
    };
}

function broadcastState() {
    wss.clients.forEach((client) => {
        if (client.readyState === 1) { // WebSocket.OPEN
            const state = client.isAdmin ? getAdminState(client.sessionId) : getPublicState(client.sessionId);
            client.send(JSON.stringify({ type: 'state', data: state }));
        }
    });
}

wss.on('connection', (ws, req) => {
    // Parse query params
    const url = new URL(req.url, 'http://localhost');
    const perfil = (url.searchParams.get('perfil') || 'votante').toLowerCase();
    const sessionId = url.searchParams.get('sessionId') || uuidv4();

    ws.sessionId = sessionId;
    ws.isAdmin = perfil === 'admin';
    ws.perfil = perfil;

    console.log(`📱 Nova conexão: ${perfil} (${sessionId.substring(0, 8)}...)`);

    // Enviar estado inicial
    const state = ws.isAdmin ? getAdminState(sessionId) : getPublicState(sessionId);
    ws.send(JSON.stringify({ type: 'state', data: state }));

    ws.on('message', (message) => {
        try {
            const msg = JSON.parse(message);

            switch (msg.action) {
                case 'votar':
                    if (votar(sessionId, msg.idCandidato)) {
                        console.log(`🗳️ Voto registrado: candidato ${msg.idCandidato}`);
                        broadcastState();
                    }
                    break;

                case 'abrir-votacao':
                    if (ws.isAdmin && abrirVotacao()) {
                        console.log('🟢 Votação aberta');
                        broadcastState();
                    }
                    break;

                case 'fechar-votacao':
                    if (ws.isAdmin && fecharVotacao()) {
                        console.log('🔒 Votação fechada');
                        broadcastState();
                    }
                    break;

                case 'revelar':
                    if (ws.isAdmin && revelarVencedor()) {
                        console.log('🏆 Vencedor revelado');
                        broadcastState();
                    }
                    break;

                case 'proxima':
                    if (ws.isAdmin && proximaCategoria()) {
                        console.log('⏭️ Próxima categoria');
                        broadcastState();
                    }
                    break;

                case 'resetar':
                    if (ws.isAdmin && resetarCategoria()) {
                        console.log('🔄 Categoria resetada');
                        broadcastState();
                    }
                    break;
            }
        } catch (e) {
            console.error('Erro ao processar mensagem:', e);
        }
    });

    ws.on('close', () => {
        console.log(`👋 Desconexão: ${perfil}`);
    });
});

// =============================================================================
// EXPRESS - Servir arquivos estáticos
// =============================================================================

app.use(express.static(join(__dirname, 'resources', 'public')));

app.get('/', (req, res) => {
    res.sendFile(join(__dirname, 'resources', 'public', 'index.html'));
});

// =============================================================================
// INICIAR SERVIDOR
// =============================================================================

const PORT = process.env.PORT || 8080;

server.listen(PORT, () => {
    console.log('');
    console.log('🏆 ═══════════════════════════════════════════════════════════');
    console.log('   PLATAFORMA DE VOTAÇÃO - MELHORES DO ANO');
    console.log('═══════════════════════════════════════════════════════════════');
    console.log('');
    console.log(`📱 Votante:      http://localhost:${PORT}/?perfil=votante`);
    console.log(`🎛️  Admin:        http://localhost:${PORT}/?perfil=admin`);
    console.log(`🎬 Apresentador: http://localhost:${PORT}/?perfil=apresentador`);
    console.log('');
    console.log('═══════════════════════════════════════════════════════════════');
    console.log('');
});
