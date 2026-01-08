(ns votacao.ui.apresentador
  "Visão do Apresentador - Interface para telão/projetor.
   Design minimalista, alto contraste, animações na revelação."
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [votacao.state :as state]))

;; =============================================================================
;; Componentes de Tela
;; =============================================================================

(e/defn TelaAguardando
  "Tela de espera entre categorias."
  [nome-categoria]
  (e/client
    (dom/div
      (dom/props {:class "flex flex-col items-center justify-center min-h-screen text-center px-12"})
      
      ;; Logo/Título
      (dom/div
        (dom/props {:class "mb-12"})
        (dom/h1
          (dom/props {:class "text-6xl md:text-8xl font-display font-bold gradient-text mb-4"})
          (dom/text "🏆"))
        (dom/h2
          (dom/props {:class "text-4xl md:text-5xl font-display text-white"})
          (dom/text "Melhores do Ano")))
      
      ;; Próxima categoria
      (dom/div
        (dom/props {:class "glass-card rounded-3xl px-12 py-8"})
        (dom/p
          (dom/props {:class "text-2xl text-midnight-400 mb-4"})
          (dom/text "Próxima Categoria"))
        (dom/h3
          (dom/props {:class "text-4xl md:text-6xl font-display font-bold gradient-text"})
          (dom/text nome-categoria))))))

(e/defn TelaVotacaoAberta
  "Tela exibida durante a votação - incentiva participação."
  [nome-categoria]
  (e/client
    (dom/div
      (dom/props {:class "flex flex-col items-center justify-center min-h-screen text-center px-12"})
      
      ;; Indicador de votação ativa
      (dom/div
        (dom/props {:class "mb-12"})
        (dom/div
          (dom/props {:class "inline-flex items-center gap-4 px-8 py-4 rounded-full 
                             bg-green-500/20 border-2 border-green-500 animate-pulse"})
          (dom/span
            (dom/props {:class "w-4 h-4 rounded-full bg-green-500"})
            (dom/text ""))
          (dom/span
            (dom/props {:class "text-2xl font-bold text-green-400"})
            (dom/text "VOTAÇÃO ABERTA"))))
      
      ;; Categoria
      (dom/h2
        (dom/props {:class "text-5xl md:text-7xl font-display font-bold text-white mb-8"})
        (dom/text nome-categoria))
      
      ;; Call to action
      (dom/div
        (dom/props {:class "space-y-6"})
        (dom/p
          (dom/props {:class "text-3xl md:text-4xl gradient-text font-bold"})
          (dom/text "Vote Agora!"))
        
        ;; Placeholder para QR Code
        (dom/div
          (dom/props {:class "glass-card rounded-2xl p-8 inline-block"})
          (dom/div
            (dom/props {:class "w-48 h-48 bg-white rounded-xl flex items-center justify-center"})
            (dom/div
              (dom/props {:class "text-center text-midnight-950"})
              (dom/p
                (dom/props {:class "text-sm font-medium"})
                (dom/text "📱 QR CODE"))
              (dom/p
                (dom/props {:class "text-xs text-midnight-600 mt-1"})
                (dom/text "Escaneie para votar")))))
        
        (dom/p
          (dom/props {:class "text-xl text-midnight-400"})
          (dom/text "ou acesse: votacao.local/?perfil=votante"))))))

(e/defn TelaVotacaoFechada
  "Tela de processamento após fechamento da votação."
  [nome-categoria]
  (e/client
    (dom/div
      (dom/props {:class "flex flex-col items-center justify-center min-h-screen text-center px-12"})
      
      ;; Spinner elegante
      (dom/div
        (dom/props {:class "mb-12"})
        (dom/div
          (dom/props {:class "relative"})
          (dom/div
            (dom/props {:class "w-32 h-32 rounded-full border-4 border-midnight-700"})
            (dom/text ""))
          (dom/div
            (dom/props {:class "absolute inset-0 w-32 h-32 rounded-full border-4 border-transparent 
                               border-t-gold-400 animate-spin"})
            (dom/text ""))))
      
      ;; Categoria
      (dom/h2
        (dom/props {:class "text-4xl md:text-5xl font-display text-white mb-6"})
        (dom/text nome-categoria))
      
      ;; Mensagem
      (dom/p
        (dom/props {:class "text-2xl text-midnight-400"})
        (dom/text "Contabilizando votos..."))
      
      (dom/p
        (dom/props {:class "text-xl text-gold-400 mt-4 animate-pulse"})
        (dom/text "Aguarde a revelação")))))

(e/defn TelaRevelacao
  "Tela de revelação do vencedor com animação."
  [categoria vencedor]
  (e/client
    (dom/div
      (dom/props {:class "flex flex-col items-center justify-center min-h-screen text-center px-12"})
      
      ;; Confetes/Efeito visual (CSS apenas)
      (dom/div
        (dom/props {:class "absolute inset-0 overflow-hidden pointer-events-none"})
        (dom/div
          (dom/props {:class "absolute top-0 left-1/4 w-2 h-2 bg-gold-400 rounded-full animate-bounce"
                      :style {:animation-delay "0s"}})
          (dom/text ""))
        (dom/div
          (dom/props {:class "absolute top-0 right-1/4 w-2 h-2 bg-gold-500 rounded-full animate-bounce"
                      :style {:animation-delay "0.2s"}})
          (dom/text ""))
        (dom/div
          (dom/props {:class "absolute top-0 left-1/2 w-2 h-2 bg-gold-300 rounded-full animate-bounce"
                      :style {:animation-delay "0.4s"}})
          (dom/text "")))
      
      ;; Categoria
      (dom/div
        (dom/props {:class "mb-8"})
        (dom/p
          (dom/props {:class "text-2xl text-midnight-400 mb-2"})
          (dom/text "E o vencedor de"))
        (dom/h3
          (dom/props {:class "text-3xl md:text-4xl font-display text-white"})
          (dom/text (:nome categoria))))
      
      ;; Troféu animado
      (dom/div
        (dom/props {:class "mb-8 animate-reveal"})
        (dom/span
          (dom/props {:class "text-8xl md:text-9xl"})
          (dom/text "🏆")))
      
      ;; Nome do vencedor
      (dom/div
        (dom/props {:class "animate-reveal"
                    :style {:animation-delay "0.3s"}})
        (dom/h2
          (dom/props {:class "text-5xl md:text-7xl lg:text-8xl font-display font-bold 
                             gradient-text animate-pulse-gold"})
          (dom/text (or (:nome vencedor) "Empate!"))))
      
      ;; Brilho decorativo
      (dom/div
        (dom/props {:class "mt-8 flex items-center gap-4 justify-center"})
        (dom/span
          (dom/props {:class "text-gold-400 text-4xl"})
          (dom/text "✨"))
        (dom/span
          (dom/props {:class "text-gold-500 text-5xl"})
          (dom/text "⭐"))
        (dom/span
          (dom/props {:class "text-gold-400 text-4xl"})
          (dom/text "✨"))))))

;; =============================================================================
;; Componente Principal
;; =============================================================================

(e/defn VisaoApresentador
  "Visão para telão/projetor.
   Nunca mostra contagem de votos - apenas o vencedor final."
  []
  (e/client
    (dom/div
      (dom/props {:class "min-h-screen bg-midnight-950 overflow-hidden"})
      
      (e/server
        (let [state @state/!game-state
              fase (:fase state)
              categoria (state/categoria-atual state)
              id-cat (:id categoria)
              vencedor (state/vencedor-categoria state id-cat)]
          
          (e/client
            (case fase
              :aguardando
              (TelaAguardando. (:nome categoria))
              
              :votacao-aberta
              (TelaVotacaoAberta. (:nome categoria))
              
              :votacao-fechada
              (TelaVotacaoFechada. (:nome categoria))
              
              :revelacao
              (TelaRevelacao. categoria vencedor)
              
              ;; Fallback
              (TelaAguardando. (:nome categoria)))))))))
