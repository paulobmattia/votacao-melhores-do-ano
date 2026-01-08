(ns votacao.state
  "Estado global da aplicação de votação.
   Este namespace define a Fonte Única da Verdade (Single Source of Truth)
   e todas as funções de transição de estado."
  #?(:cljs (:require-macros [votacao.state :refer [on-server]])))

;; =============================================================================
;; MODELO DE DADOS - Átomo de Estado Global
;; =============================================================================

(defonce !game-state
  (atom {:fase :aguardando
         :indice-categoria-atual 0
         
         :categorias 
         [{:id 1
           :nome "Melhor Filme"
           :candidatos [{:id 101 :nome "Oppenheimer" :imagem nil}
                        {:id 102 :nome "Barbie" :imagem nil}
                        {:id 103 :nome "Poor Things" :imagem nil}
                        {:id 104 :nome "Anatomia de uma Queda" :imagem nil}]}
          
          {:id 2
           :nome "Melhor Direção"
           :candidatos [{:id 201 :nome "Christopher Nolan" :imagem nil}
                        {:id 202 :nome "Greta Gerwig" :imagem nil}
                        {:id 203 :nome "Yorgos Lanthimos" :imagem nil}
                        {:id 204 :nome "Martin Scorsese" :imagem nil}]}
          
          {:id 3
           :nome "Melhor Ator"
           :candidatos [{:id 301 :nome "Cillian Murphy" :imagem nil}
                        {:id 302 :nome "Paul Giamatti" :imagem nil}
                        {:id 303 :nome "Bradley Cooper" :imagem nil}
                        {:id 304 :nome "Leonardo DiCaprio" :imagem nil}]}
          
          {:id 4
           :nome "Melhor Atriz"
           :candidatos [{:id 401 :nome "Emma Stone" :imagem nil}
                        {:id 402 :nome "Margot Robbie" :imagem nil}
                        {:id 403 :nome "Lily Gladstone" :imagem nil}
                        {:id 404 :nome "Sandra Hüller" :imagem nil}]}
          
          {:id 5
           :nome "Melhor Trilha Sonora"
           :candidatos [{:id 501 :nome "Oppenheimer - Ludwig Göransson" :imagem nil}
                        {:id 502 :nome "Barbie - Mark Ronson" :imagem nil}
                        {:id 503 :nome "Killers of the Flower Moon" :imagem nil}
                        {:id 504 :nome "Poor Things - Jerskin Fendrix" :imagem nil}]}]
         
         :votos {}              ; {:id-categoria {:id-candidato count}}
         :sessoes-votaram #{}})) ; IDs que votaram na categoria atual

;; =============================================================================
;; FUNÇÕES AUXILIARES
;; =============================================================================

(defn categoria-atual
  "Retorna a categoria atualmente ativa."
  [state]
  (get-in state [:categorias (:indice-categoria-atual state)]))

(defn id-categoria-atual
  "Retorna o ID da categoria atualmente ativa."
  [state]
  (:id (categoria-atual state)))

(defn usuario-ja-votou?
  "Verifica se um usuário (session-id) já votou na categoria atual."
  [state session-id]
  (contains? (:sessoes-votaram state) session-id))

(defn votos-categoria
  "Retorna os votos de uma categoria específica."
  [state id-categoria]
  (get-in state [:votos id-categoria] {}))

(defn total-votos-categoria
  "Retorna o total de votos de uma categoria."
  [state id-categoria]
  (reduce + 0 (vals (votos-categoria state id-categoria))))

(defn vencedor-categoria
  "Retorna o candidato vencedor de uma categoria."
  [state id-categoria]
  (let [votos (votos-categoria state id-categoria)
        categoria (first (filter #(= (:id %) id-categoria) (:categorias state)))]
    (when (seq votos)
      (let [id-vencedor (key (apply max-key val votos))]
        (first (filter #(= (:id %) id-vencedor) (:candidatos categoria)))))))

(defn porcentagem-votos
  "Calcula a porcentagem de votos de um candidato."
  [state id-categoria id-candidato]
  (let [total (total-votos-categoria state id-categoria)
        votos-candidato (get-in state [:votos id-categoria id-candidato] 0)]
    (if (zero? total)
      0
      (Math/round (* 100.0 (/ votos-candidato total))))))

;; =============================================================================
;; TRANSIÇÕES DE ESTADO (Funções puras para uso com swap!)
;; =============================================================================

(defn registrar-voto
  "Registra um voto para um candidato. Retorna o novo estado.
   IDEMPOTENTE: Se o usuário já votou, retorna o estado sem modificação."
  [state session-id id-candidato]
  (let [id-cat (id-categoria-atual state)]
    (if (or (not= (:fase state) :votacao-aberta)
            (usuario-ja-votou? state session-id))
      state  ; Retorna sem modificação se não pode votar
      (-> state
          (update-in [:votos id-cat id-candidato] (fnil inc 0))
          (update :sessoes-votaram conj session-id)))))

(defn abrir-votacao
  "Transição: :aguardando -> :votacao-aberta"
  [state]
  (if (= (:fase state) :aguardando)
    (assoc state :fase :votacao-aberta)
    state))

(defn fechar-votacao
  "Transição: :votacao-aberta -> :votacao-fechada"
  [state]
  (if (= (:fase state) :votacao-aberta)
    (assoc state :fase :votacao-fechada)
    state))

(defn revelar-vencedor
  "Transição: :votacao-fechada -> :revelacao"
  [state]
  (if (= (:fase state) :votacao-fechada)
    (assoc state :fase :revelacao)
    state))

(defn proxima-categoria
  "Avança para a próxima categoria e reseta a fase."
  [state]
  (let [proximo-indice (inc (:indice-categoria-atual state))
        total-categorias (count (:categorias state))]
    (if (< proximo-indice total-categorias)
      (-> state
          (assoc :indice-categoria-atual proximo-indice)
          (assoc :fase :aguardando)
          (assoc :sessoes-votaram #{}))
      state)))  ; Já está na última categoria

(defn resetar-categoria
  "Reseta a votação da categoria atual."
  [state]
  (let [id-cat (id-categoria-atual state)]
    (-> state
        (assoc :fase :aguardando)
        (assoc-in [:votos id-cat] {})
        (assoc :sessoes-votaram #{}))))

;; =============================================================================
;; FUNÇÕES DE AÇÃO (Mutam o átomo via swap!)
;; =============================================================================

(defn votar!
  "Registra um voto de forma atômica e thread-safe."
  [session-id id-candidato]
  (swap! !game-state registrar-voto session-id id-candidato))

(defn abrir-votacao!
  "Abre a votação para a categoria atual."
  []
  (swap! !game-state abrir-votacao))

(defn fechar-votacao!
  "Fecha a votação da categoria atual."
  []
  (swap! !game-state fechar-votacao))

(defn revelar-vencedor!
  "Revela o vencedor da categoria atual."
  []
  (swap! !game-state revelar-vencedor))

(defn proxima-categoria!
  "Avança para a próxima categoria."
  []
  (swap! !game-state proxima-categoria))

(defn resetar-categoria!
  "Reseta a votação da categoria atual."
  []
  (swap! !game-state resetar-categoria))
