package ed.mois.models.storm.karrlike

import ed.mois.core.storm.strategies._
import ed.mois.core.storm._

class P1 extends ProcessTemplate {
    def name = "P1"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(314)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P2 extends ProcessTemplate {
    def name = "P2"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(228)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P3 extends ProcessTemplate {
    def name = "P3"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(107)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P4 extends ProcessTemplate {
    def name = "P4"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(48)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P5 extends ProcessTemplate {
    def name = "P5"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(37)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P6 extends ProcessTemplate {
    def name = "P6"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(37)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P7 extends ProcessTemplate {
    def name = "P7"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(27)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P8 extends ProcessTemplate {
    def name = "P8"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(21)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P9 extends ProcessTemplate {
    def name = "P9"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(17)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P10 extends ProcessTemplate {
    def name = "P10"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(15)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P11 extends ProcessTemplate {
    def name = "P11"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(14)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P12 extends ProcessTemplate {
    def name = "P12"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(11)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P13 extends ProcessTemplate {
    def name = "P13"
    override def evolve(state: KarrlikeState, t: Double, dt: Double) { 
        try {Thread.sleep(10)} catch {case e: Exception => }
        super.evolve(state, t, dt)
    }
}

class P14 extends ProcessTemplate {
    def name = "P14"
}

class P15 extends ProcessTemplate {
    def name = "P15"
}

class P16 extends ProcessTemplate {
    def name = "P16"
}

class P17 extends ProcessTemplate {
    def name = "P17"
}

class P18 extends ProcessTemplate {
    def name = "P18"
}

class P19 extends ProcessTemplate {
    def name = "P19"
}

class P20 extends ProcessTemplate {
    def name = "P20"
}

class P21 extends ProcessTemplate {
    def name = "P21"
}

class P22 extends ProcessTemplate {
    def name = "P22"
}

class P23 extends ProcessTemplate {
    def name = "P23"
}

class P24 extends ProcessTemplate {
    def name = "P24"
}

class P25 extends ProcessTemplate {
    def name = "P25"
}

class P26 extends ProcessTemplate {
    def name = "P26"
}

