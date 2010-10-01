
(ns jinrou-clojure.util.captcha
  (:refer-clojure)
  (:use sandbar.stateful-session)
  (:import (java.awt Graphics RenderingHints Color Font)
    (java.awt.image BufferedImage)
    (java.awt.font FontRenderContext)
    (java.io ByteArrayInputStream ByteArrayOutputStream ObjectOutputStream)
    (java.security SecureRandom)
    (javax.imageio ImageIO)))

(def *big5-commons* 5401)
(def *captcha-length* 3)

(def *captcha-random* (new SecureRandom))
(def *captcha-chars*  (char-array *big5-commons*))

(defn- coerce-big5 [buffer hi low]
  (let [hi  (if (>=  hi 128) (- hi 256) hi)
        low (if (>= low 128) (- low 256) low)]
    (aset buffer 0 (byte hi))
    (aset buffer 1 (byte low))
    (.charAt (String. buffer "Big5") 0)))

(defn- init-buffer! []
  (let [tmp-bytes (byte-array 2)
        index     (atom -1)
        insert-char!  (fn [c]
                       (aset *captcha-chars* (swap! index inc) c)
                        #_(println @index))
        make-buffer! (fn [hi1 hi2 low1 low2] 
                       (doseq [hi (range hi1 (inc hi2))]
                         (doseq [low (range low1 (inc low2))]
                           (insert-char! (coerce-big5 tmp-bytes hi low)))))]
    (make-buffer! 0xa4 0xc5 0x40 0x7e)
    (make-buffer! 0xa4 0xc5 0xa1 0xfe)
    (make-buffer! 0xc6 0xc6 0x40 0x7e)))

(init-buffer!)

(defn produce-captcha []
  (apply str (map (fn [_] (aget *captcha-chars* (.nextInt *captcha-random* *big5-commons*)))
               (range *captcha-length*))))

;
; 以下為繪圖部份
;
(def *captcha-default-width* 180)
(def *captcha-default-height* 50)
(def *captcha-default-fonts* (map #(Font. % Font/BOLD, 40)
                               [ ;"新細明體", "細明體", "標楷體", "微軟正黑體", 
                               "王漢宗中明體注音","王漢宗中楷體注音"]))
(def *captcha-default-color* Color/BLACK)

(defn- fish-eye-formula [s]
  (cond
    (< s 0.0) 0.0
    (> s 1.0) s
    :else (+ (* -0.75 s s s) (* 1.5 s s) (* 0.25 s))))

(defn- rand-int- [i j]
  (let [k (- j i)]
    (+ (rand-int k) i)))

(def *captcha-gimp-vcolor* Color/WHITE)
(def *captcha-gimp-hcolor* Color/WHITE)
(defn- gimp [#^BufferedImage image]
  (let [width  (.getWidth  image)
        height (.getHeight image)
        vstripes (quot width 20)
        hstripes (quot height 20)
        vspace   (quot width (inc vstripes))
        hspace   (quot height (inc hstripes))
        graph    (.getGraphics image)
        pix      (int-array (* width height))]
    ; Draw the horizontal stripes
    (doseq [i (range hspace (dec height) hspace)]
      (.setColor graph *captcha-gimp-hcolor*)
      (.drawLine graph 0 i width i))

    ; Draw the vertical stripes
    (doseq [i (range vspace (dec width) vspace)]
      (.setColor graph *captcha-gimp-vcolor*)
      (.drawLine graph i 0 i height))

    ; Create a pixel array of the original image.
    ; we need this later to do the operations on.
    (let [j   (atom -1)]
      (doseq [j1 (range width)]
        (doseq [k1 (range height)]
          (aset pix (swap! j inc) (.getRGB image j1 k1)))))

    (let [distance (rand-int- (quot width 4) (quot width 3))
          w-mid    (quot width 2)
          h-mid    (quot height 2)]
      (doseq [x (range width)]
        (doseq [y (range height)]
          (let [rel-x (- x w-mid)
                rel-y (- y h-mid)
                d1    (Math/sqrt (+ (* rel-x rel-x) (* rel-y rel-y)))]
            (when (< d1 distance)
              (let [j2 (int (+ w-mid
                         (* (/ (* (fish-eye-formula (/ d1 distance)) distance) d1)
                           (- x w-mid))))
                    k2 (int (+ w-mid
                         (* (/ (* (fish-eye-formula (/ d1 distance)) distance) d1)
                           (- y h-mid))))]
                (.setRGB image x y (aget pix (+ (* j2 height) k2)))))))))
    (.dispose graph)))

(def *captcha-X1* 0)
(def *captcha-line-thickness* 3)
(defn- draw-line [#^Graphics g y1 x2 y2]
  (let [dx0 (- x2 *captcha-X1*)
        dy0 (- y2 y1)
        line-length (Math/sqrt (+ (* dx0 dx0) (* dy0 dy0)))
        scale       (/ *captcha-line-thickness* (* line-length 2))
        ddx0        (* (- scale) dy0)
        ddy0        (* scale dx0)
        ddx         (+ ddx0 (if (> ddx0 0) 0.5 -0.5))
        ddy         (+ ddy0 (if (> ddy0 0) 0.5 -0.5))
        dx          (int ddx)
        dy          (int ddy)
        x-points    (int-array 4)
        y-points    (int-array 4)]
    (.setColor g *captcha-default-color*)
    (aset x-points 0 (+ *captcha-X1* dx))
    (aset y-points 0 (+ y1 dy))
    (aset x-points 1 (- *captcha-X1* dx))
    (aset y-points 1 (- y1 dy))
    (aset x-points 2 (- x2 dx))
    (aset y-points 2 (- y2 dy))
    (aset x-points 3 (+ x2 dx))
    (aset y-points 3 (+ y2 dy))
    (.fillPolygon x-points y-points 4)))

(defn- make-noise [#^BufferedImage image]
  (let [graphics (.createGraphics image)
        width    (.getWidth image)
        height   (.getHeight image)
        y1       (rand-int- 1 height)
        y2       (rand-int- 1 height)]
    (draw-line graphics y1 width y2)))

(defn render [#^String word #^BufferedImage image]
  (let [g     (.createGraphics image)
        hints (RenderingHints. RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
        frc   (.getFontRenderContext g)
        wc    (.toCharArray word)
        start-pos-x (atom 0)]
    (.add hints (RenderingHints. RenderingHints/KEY_RENDERING RenderingHints/VALUE_RENDER_QUALITY))
    (.setRenderingHints g hints)
    (.setColor g *captcha-default-color*)

    (doseq [element wc]
      (let [itchar (char-array 1 element)
            itfont (rand-nth *captcha-default-fonts*)
            gv     (.createGlyphVector itfont frc itchar)
            char-width (.. gv getVisualBounds getWidth)]
        (.setFont g itfont)
        (.drawChars g itchar 0 1 @start-pos-x 35)
        (swap! start-pos-x #(+ (int char-width ) %))))

    ;val noises = generator.nextInt(3) + 1
    ;for (i <- 1 to noises)
    ;  makeNoise(image)
    ;gimp(image)
  ))

(def *captcha-answer-session-key* "captcha-answer")

(defn captcha-answer []
  (session-get *captcha-answer-session-key* ""))

(defn captcha-view []
  (let [image (BufferedImage. *captcha-default-width* *captcha-default-height* BufferedImage/TYPE_INT_ARGB)
        answer (produce-captcha)
        g   (.createGraphics image)
        outstream (new ByteArrayOutputStream)]
      ;g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    (.drawImage g image nil nil)
    (session-put! *captcha-answer-session-key* answer)
    (render answer image)

    (ImageIO/write image "png" (ImageIO/createImageOutputStream outstream))
    { :headers { "Content-Type"  "image/png" "Cache-Control" "private,no-cache,no-store"}
      :status 200 :body (ByteArrayInputStream. (.toByteArray outstream))}))
