(ns votacao.ui.admin
  "Visão do Admin - Painel de controle para gerenciamento da votação.
   Componente Electric Clojure com controles de transição de estado."
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [votacao.state :as state]))

;; =============================================================================
;; Componentes Auxiliares
;; =============================================================================

(e/defn IndicadorFase
  "Exibe a fase atual com cor e ícone correspondente."
  [fase]
  (e/client
    (let [[cor icone texto] (case fase
                              :aguardando ["bg-midnight-600" "⏳" "Aguardando"]
                              :votacao-aberta ["bg-green-600" "🟢" "Votação Aberta"]
                              :votacao-fechada ["bg-amber-600" "🔒" "Votação Fechada"]
                              :revelacao ["bg-gold-600" "🏆" "Revelação"]
                              ["bg-midnight-600" "❓" "Desconhecido"])]
      (dom/div
        (dom/props {:class (str "inline-flex items-center gap-2 px-4 py-2 rounded-full " cor)})
        (dom/span (dom/text icone))
        (dom/span
          (dom/props {:class "font-medium text-white"})
          (dom/text texto))))))

(e/defn BotaoControle
  "Botão de controle do admin."
  [texto icone classe-extra acao habilitado?]
  (e/client
    (dom/button
      (dom/props {:class (str "flex items-center justify-center gap-2 px-6 py-3 rounded-xl "
                              "font-semibold transition-all duration-200 "
                              (if habilitado?
                                (str "cursor-pointer hover:scale-105 active:scale-95 " classe-extra)
                                "bg-midnight-800 text-midnight-500 cursor-not-allowed"))
                  :disabled (not habilitado?)})
      (dom/on "click"
        (e/fn [_]
          (when habilitado?
            (e/server (acao)))))
      (dom/span (dom/text icone))
      (dom/span (dom/text texto)))))

(e/defn BarraProgresso
  "Barra de progresso para exibir porcentagem de votos."
  [porcentagem cor-classe]
  (e/client
    (dom/div
      (dom/props {:class "w-full h-3 bg-midnight-800 rounded-full overflow-hidden"})
      (dom/div
        (dom/props {:class (str "h-full transition-all duration-500 " cor-classe)
                    :style {:width (str porcentagem "%")}})))))

(e/defn CardCandidato
  "Card exibindo candidato com contagem de votos."
  [candidato votos-candidato total-votos posicao]
  (e/client
    (let [porcentagem (if (zero? total-votos) 
                        0 
                        (Math/round (* 100.0 (/ votos-candidato total-votos))))
          cor-barra (case posicao
                      0 "bg-gradient-to-r from-gold-400 to-gold-600"
                      1 "bg-gradient-to-r from-slate-300 to-slate-400"
                      2 "bg-gradient-to-r from-amber-600 to-amber-700"
                      "bg-midnight-500")]
      (dom/div
        (dom/props {:class "glass-card rounded-xl p-4"})
        
        (dom/div
          (dom/props {:class "flex items-center justify-between mb-3"})
          
          (dom/div
            (dom/props {:class "flex items-center gap-3"})
            (dom/span
              (dom/props {:class (str "w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold "
                                      (case posicao
                                        0 "bg-gold-500 text-midnight-950"
                                        1 "bg-slate-400 text-midnight-950"
                                        2 "bg-amber-600 text-midnight-950"
                                        "bg-midnight-600 text-white"))})
              (dom/text (str (inc posicao))))
            (dom/span
              (dom/props {:class "font-medium text-white"})
              (dom/text (:nome candidato))))
          
          (dom/div
            (dom/props {:class "text-right"})
            (dom/span
              (dom/props {:class "text-2xl font-bold text-white"})
              (dom/text (str votos-candidato)))
            (dom/span
              (dom/props {:class "text-midnight-400 text-sm ml-2"})
              (dom/text (str "(" porcentagem "%)")))))
        
        (BarraProgresso. porcentagem cor-barra)))))

;; =============================================================================
;; Componente Principal
;; =============================================================================

(e/defn VisaoAdmin
  "Painel de controle do administrador.
   Permite controlar o fluxo da votação e visualizar resultados em tempo real."
  []
  (e/client
    (dom/div
      (dom/props {:class "min-h-screen bg-midnight-950 p-6"})
      
      ;; Header
      (dom/header
        (dom/props {:class "mb-8"})
        (dom/div
          (dom/props {:class "flex items-center justify-between"})
          (dom/div
            (dom/h1
              (dom/props {:class "text-3xl font-display font-bold gradient-text"})
              (dom/text "🎛️ Painel Admin"))
            (dom/p
              (dom/props {:class "text-midnight-400 mt-1"})
              (dom/text "Controle da Votação")))
          
          (e/server
            (let [fase (:fase @state/!game-state)]
              (e/client (IndicadorFase. fase))))))
      
      (e/server
        (let [state @state/!game-state
              fase (:fase state)
              categoria (state/categoria-atual state)
              id-cat (:id categoria)
              votos-cat (state/votos-categoria state id-cat)
              total-votos (state/total-votos-categoria state id-cat)
              indice-atual (:indice-categoria-atual state)
              total-categorias (count (:categorias state))
              
              ;; Ordenar candidatos por votos
              candidatos-ordenados (sort-by #(get votos-cat (:id %) 0) > (:candidatos categoria))]
          
          (e/client
            (dom/div
              (dom/props {:class "grid gap-6 lg:grid-cols-2"})
              
              ;; Coluna Esquerda - Controles
              (dom/div
                (dom/props {:class "space-y-6"})
                
                ;; Card de Categoria Atual
                (dom/div
                  (dom/props {:class "glass-card rounded-2xl p-6"})
                  (dom/div
                    (dom/props {:class "flex items-center justify-between mb-4"})
                    (dom/h2
                      (dom/props {:class "text-xl font-bold text-white"})
                      (dom/text "Categoria Atual"))
                    (dom/span
                      (dom/props {:class "text-midnight-400"})
                      (dom/text (str (inc indice-atual) "/" total-categorias))))
                  
                  (dom/h3
                    (dom/props {:class "text-2xl font-display gradient-text mb-4"})
                    (dom/text (:nome categoria)))
                  
                  (dom/div
                    (dom/props {:class "flex items-center gap-4 text-midnight-300"})
                    (dom/span (dom/text "📊 Total de votos:"))
                    (dom/span
                      (dom/props {:class "text-2xl font-bold text-gold-400"})
                      (dom/text (str total-votos)))))
                
                ;; Controles de Fluxo
                (dom/div
                  (dom/props {:class "glass-card rounded-2xl p-6"})
                  (dom/h2
                    (dom/props {:class "text-xl font-bold text-white mb-4"})
                    (dom/text "Controles"))
                  
                  (dom/div
                    (dom/props {:class "grid grid-cols-2 gap-4"})
                    
                    (BotaoControle. 
                      "Abrir Votação" "▶️"
                      "bg-green-600 hover:bg-green-500 text-white"
                      state/abrir-votacao!
                      (= fase :aguardando))
                    
                    (BotaoControle.
                      "Fechar Votação" "⏹️"
                      "bg-amber-600 hover:bg-amber-500 text-white"
                      state/fechar-votacao!
                      (= fase :votacao-aberta))
                    
                    (BotaoControle.
                      "Revelar Vencedor" "🏆"
                      "bg-gradient-to-r from-gold-500 to-gold-600 hover:from-gold-400 hover:to-gold-500 text-midnight-950"
                      state/revelar-vencedor!
                      (= fase :votacao-fechada))
                    
                    (BotaoControle.
                      "Próxima Categoria" "⏭️"
                      "bg-blue-600 hover:bg-blue-500 text-white"
                      state/proxima-categoria!
                      (and (= fase :revelacao)
                           (< indice-atual (dec total-categorias)))))
                  
                  (dom/div
                    (dom/props {:class "mt-4 pt-4 border-t border-midnight-700"})
                    (BotaoControle.
                      "Resetar Categoria" "🔄"
                      "bg-red-600/20 hover:bg-red-600 text-red-400 hover:text-white border border-red-600/50"
                      state/resetar-categoria!
                      true))))
              
              ;; Coluna Direita - Ranking em Tempo Real
              (dom/div
                (dom/props {:class "glass-card rounded-2xl p-6"})
                (dom/h2
                  (dom/props {:class "text-xl font-bold text-white mb-4"})
                  (dom/text "📊 Ranking ao Vivo"))
                
                (dom/div
                  (dom/props {:class "space-y-4"})
                  (e/for-by :id [[idx candidato] (map-indexed vector candidatos-ordenados)]
                    (let [votos-c (get votos-cat (:id candidato) 0)]
                      (CardCandidato. candidato votos-c total-votos idx))))))))))))
