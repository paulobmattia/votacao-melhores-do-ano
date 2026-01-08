(ns votacao.ui.votante
  "Visão do Votante - Interface mobile-first para votação.
   Componente Electric Clojure com sincronização reativa."
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [votacao.state :as state]))

;; =============================================================================
;; Componentes Auxiliares
;; =============================================================================

(e/defn BotaoCandidato
  "Botão de votação para um candidato individual."
  [candidato session-id ja-votou?]
  (e/client
    (let [id (:id candidato)
          nome (:nome candidato)]
      (dom/button
        (dom/props {:class (str "vote-button w-full py-6 px-8 rounded-2xl text-xl font-semibold "
                                "transition-all duration-300 transform "
                                (if ja-votou?
                                  "bg-midnight-700 text-midnight-400 cursor-not-allowed opacity-50"
                                  "glass-card text-white hover:border-gold-400 hover:shadow-gold-400/20 "
                                  "active:scale-95"))
                    :disabled ja-votou?})
        (dom/on "click"
          (e/fn [_]
            (when-not ja-votou?
              (e/server
                (state/votar! session-id id)))))
        (dom/text nome)))))

(e/defn TelaAguardando
  "Tela exibida quando a votação não está aberta."
  [fase nome-categoria]
  (e/client
    (dom/div
      (dom/props {:class "flex flex-col items-center justify-center min-h-[60vh] text-center px-8"})
      
      ;; Ícone animado
      (dom/div
        (dom/props {:class "mb-8"})
        (dom/div
          (dom/props {:class "w-24 h-24 rounded-full bg-midnight-800 flex items-center justify-center mx-auto"})
          (dom/span
            (dom/props {:class "text-4xl"})
            (dom/text (case fase
                        :aguardando "⏳"
                        :votacao-fechada "🔒"
                        :revelacao "🏆"
                        "⏳")))))
      
      ;; Mensagem
      (dom/h2
        (dom/props {:class "text-2xl font-bold text-white mb-4"})
        (dom/text (case fase
                    :aguardando "Aguarde..."
                    :votacao-fechada "Votação Encerrada"
                    :revelacao "Resultado Anunciado!"
                    "Aguarde...")))
      
      (dom/p
        (dom/props {:class "text-midnight-400 text-lg"})
        (dom/text (case fase
                    :aguardando "A próxima categoria será liberada em breve."
                    :votacao-fechada "Os votos estão sendo contabilizados."
                    :revelacao (str "Confira o vencedor de: " nome-categoria)
                    "Por favor, aguarde."))))))

(e/defn TelaVotoRegistrado
  "Confirmação de voto registrado com sucesso."
  []
  (e/client
    (dom/div
      (dom/props {:class "flex flex-col items-center justify-center py-12 text-center"})
      
      (dom/div
        (dom/props {:class "w-20 h-20 rounded-full bg-gradient-to-br from-gold-400 to-gold-600 
                           flex items-center justify-center mb-6 animate-pulse-gold"})
        (dom/span
          (dom/props {:class "text-3xl"})
          (dom/text "✓")))
      
      (dom/h3
        (dom/props {:class "text-xl font-bold gradient-text mb-2"})
        (dom/text "Voto Registrado!"))
      
      (dom/p
        (dom/props {:class "text-midnight-400"})
        (dom/text "Seu voto foi contabilizado com sucesso.")))))

;; =============================================================================
;; Componente Principal
;; =============================================================================

(e/defn VisaoVotante
  "Componente principal da visão do votante.
   Reativo ao estado global do servidor."
  [session-id]
  (e/client
    (dom/div
      (dom/props {:class "min-h-screen bg-midnight-950 pb-safe"})
      
      ;; Header
      (dom/header
        (dom/props {:class "pt-8 pb-6 px-6 text-center"})
        (dom/h1
          (dom/props {:class "text-2xl font-display font-bold gradient-text mb-2"})
          (dom/text "🏆 Melhores do Ano"))
        (dom/p
          (dom/props {:class "text-midnight-400 text-sm"})
          (dom/text "Vote nos seus favoritos")))
      
      ;; Conteúdo Principal
      (dom/main
        (dom/props {:class "px-6"})
        
        (e/server
          (let [state @state/!game-state
                fase (:fase state)
                categoria (state/categoria-atual state)
                ja-votou? (state/usuario-ja-votou? state session-id)]
            
            (e/client
              (if (= fase :votacao-aberta)
                ;; Votação Aberta
                (dom/div
                  (dom/props {:class "space-y-6"})
                  
                  ;; Categoria atual
                  (dom/div
                    (dom/props {:class "text-center mb-8"})
                    (dom/span
                      (dom/props {:class "inline-block px-4 py-2 rounded-full bg-gold-500/20 
                                         text-gold-400 text-sm font-medium mb-4"})
                      (dom/text "Votação Aberta"))
                    (dom/h2
                      (dom/props {:class "text-3xl font-display font-bold text-white"})
                      (dom/text (:nome categoria))))
                  
                  ;; Lista de candidatos ou confirmação
                  (if ja-votou?
                    (TelaVotoRegistrado.)
                    (dom/div
                      (dom/props {:class "space-y-4"})
                      (e/for [candidato (:candidatos categoria)]
                        (BotaoCandidato. candidato session-id ja-votou?)))))
                
                ;; Votação Fechada
                (TelaAguardando. fase (:nome categoria))))))))))
