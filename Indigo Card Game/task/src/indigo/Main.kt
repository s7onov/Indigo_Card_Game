package indigo

import kotlin.random.Random
import kotlin.system.exitProcess

const val DECK_SIZE = 52
const val BONUS_POINTS = 3

enum class Suits(val image: Char) {
    DIAMONDS('♦'),
    HEARTS('♥'),
    SPADES('♠'),
    CLUBS('♣'),
}

enum class Ranks(val image: String, val power: Int) {
    ACE("A", 14),
    DEUCE("2", 2),
    TREY("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("J", 11),
    QUEEN("Q", 12),
    KING("K", 13);
}

data class Card(val rank: Ranks, val suit: Suits) {
    override fun toString(): String {
        return rank.image + suit.image
    }
}

private fun MutableList<Card>.reset() {
    this.clear()
    for (suit in Suits.values())
        for (rank in Ranks.values())
            this.add(Card(rank, suit))
}

open class Hand {
    val hand = mutableListOf<Card>() // or ArrayDeque<Card>()
    open fun showHand() = println(hand.joinToString(" "))
}

open class Player(val name: String) : Hand() {
    val stack = mutableListOf<Card>()
    var score = 0

    open fun play(): Card = hand.removeFirst()

    fun win(list: MutableList<Card>) {
        win(list, false)
    }

    fun win(list: MutableList<Card>, silent: Boolean) {
        if (!silent) println("$name wins cards")
        while (list.size > 0) {
            val card = list.removeFirst()
            if (card.rank.power >= 10) score++
            stack.add(card)
        }
    }
}

class Table : Hand() { // I'm a table with a hand
    fun showTable() = if (hand.size == 0) println("No cards on the table")
        else println("\n${ hand.size } cards on the table, and the top card is ${hand.last()}")
}

class Computer(name: String) : Player(name) {
    override fun play(): Card {
        val card = super.play()
        println("Computer plays $card")
        return card
    }
}

class SmartPlayer(name: String) : Player(name) {
    override fun play() : Card {
        showHand()
        return hand.removeAt(inputCardIndex() - 1)
    }

    override fun showHand() {
        print("Cards in hand:")
        for (i in hand.indices)
            print(" ${i + 1})${hand[i]}")
        println()
    }

    fun inputIsPlayerFirst() : Boolean {
        do {
            println("Play first?")
            when (readln().lowercase()) {
                "yes" -> return true
                "no" -> return false
            }
        } while (true)
    }

    private fun inputCardIndex(): Int {
        var pickedIndex = -1
        do {
            var inputIsOk = true
            println("Choose a card to play (1-${hand.size}):")
            val input = readln().lowercase()
            if (input == "exit") { println("Game over"); exitProcess(0) }
            try {
                pickedIndex = input.toInt()
                if (pickedIndex !in 1..hand.size) inputIsOk = false
            } catch (ex: Exception) { inputIsOk = false }
        } while (!inputIsOk)
        return pickedIndex
    }
}

class SmartComputer(name: String, val table: Table) : Player(name) {
    override fun play(): Card {
        showHand()
        val card = if (hand.size == 1) hand.first() else playSmart()
        hand.remove(card)
        println("Computer plays $card")
        return card
    }

    private fun playSmart(): Card {
        return if (table.hand.size == 0) pickSmart(hand)
        else {
            val last = table.hand.last()
            val candidates = mutableListOf<Card>()
            for (card in hand)
                if (card.suit == last.suit || card.rank == last.rank) candidates.add(card)
            if (candidates.size == 1) candidates.first()
            else if (candidates.size > 1) pickSmart(candidates)
            else pickSmart(hand)
        }
    }

    private fun pickSmart(list: MutableList<Card>): Card {
        val candidates = mutableSetOf<Card>()
        for (s in Suits.values()) {
            val sameSuitList = list.filter { it.suit == s }
            if (sameSuitList.size > 1) candidates.addAll(sameSuitList)
        }
        if (candidates.size > 0) return candidates.elementAt(Random.nextInt(candidates.size))
        for (r in Ranks.values()) {
            val sameRankList = list.filter { it.rank == r }
            if (sameRankList.size > 1) candidates.addAll(sameRankList)
        }
        if (candidates.size > 0) return candidates.elementAt(Random.nextInt(candidates.size))
        return list.elementAt(Random.nextInt(list.size))
    }
}

class Game {
    private val table = Table()
    private val dealer = Player("Dealer")
    private val player = SmartPlayer("Player")
    private val computer = SmartComputer("Computer", table)
    private var lastWinner : Player? = null

    private fun showStats() {
        println("Score: ${player.name} ${player.score} - ${computer.name} ${computer.score}\n" +
                "Cards: ${player.name} ${player.stack.size} - ${computer.name} ${computer.stack.size}\n")
    }

    fun start() {
        println("Indigo Card Game")
        var currentPlayer : Player = if (player.inputIsPlayerFirst()) player else computer
        val firstPlayer = currentPlayer

        dealer.hand.reset()
        dealer.hand.shuffle()
        repeat(4) { table.hand.add(dealer.play()) }
        print("Initial cards on the table: ")
        table.showHand()

        do {
            table.showTable()
            if (player.hand.isEmpty() && computer.hand.isEmpty()) {
                if (dealer.hand.size == 0) break // All the cards are dealt
                else repeat(6) {
                    player.hand.add(dealer.play())
                    computer.hand.add(dealer.play())
                }
            }
            table.hand.add(currentPlayer.play())
            if (table.hand.size > 1) {
                val last = table.hand.last()
                val preLast = table.hand[table.hand.size - 2]
                if (preLast.suit == last.suit || preLast.rank == last.rank) {
                    currentPlayer.win(table.hand)
                    lastWinner = currentPlayer
                    showStats()
                }
            }
            currentPlayer = if (currentPlayer == player) computer else player
        } while (true)

        if (lastWinner != null) lastWinner!!.win(table.hand, true)
        else firstPlayer.win(table.hand, true)
        if (player.stack.size >= computer.stack.size) {
            if (player.stack.size == DECK_SIZE / 2) firstPlayer.score += BONUS_POINTS
            else player.score += BONUS_POINTS
        } else computer.score += BONUS_POINTS
        showStats()

        println("Game Over")
    }

    /*fun stage2() {
        dealer.hand.reset()
        while (true) {
            println("Choose an action (reset, shuffle, get, exit):")
            when (readln().lowercase()) {
                "reset" -> { dealer.hand.reset(); println("Card deck is reset.") }
                "shuffle" -> { dealer.hand.shuffle(); println("Card deck is shuffled.") }
                "get" -> { dealer.hand.getCards() }
                "exit" -> { println("Bye"); break }
                else -> println("Wrong action.")
            }
        }
    }*/
}

fun main() {
    val game = Game()
    game.start()
}

/*private fun MutableList<Card>.getCards() {
    while (true) {
        println("Number of cards:")
        val number: Int
        try {
            number = readln().toInt()
        } catch (ex: Exception) { println("Invalid number of cards."); return }
        if (number !in 1..DECK_SIZE) {
            println("Invalid number of cards.")
            return
        }
        if (number > this.size) {
            println("The remaining cards are insufficient to meet the request.")
            return
        }
        println(this.take(number).joinToString(" "))
        this.removeFirst(number)
        return
    }
}*/
/*
/**
 * @param length remove index [0..length)
 */
fun <E> MutableList<E>.removeFirst(length: Int): MutableList<E> {
    if (length in 1..size) {
        subList(0, length).clear()
    }
    return this
}

/**
 * @param length remove index [(size - length)..size)
 */
fun <E> MutableList<E>.removeLast(length: Int): MutableList<E> {
    if (length in 1..size) {
        subList(size - length, size).clear()
    }
    return this
}
*/
