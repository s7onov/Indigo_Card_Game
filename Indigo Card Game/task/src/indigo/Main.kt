package indigo

import kotlin.system.exitProcess

const val DECK_SIZE = 52

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

data class Card(private val rank: Ranks, private val suit: Suits) {
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

open class Player : Hand() {
    open fun play(): Card = hand.removeFirst()
}

class Computer : Player() {
    override fun play(): Card {
        val card = super.play()
        println("Computer plays $card")
        return card
    }
}

class Table : Hand() { // I can't play, I'm a table!
    fun showTable() = println("\n${ hand.size } cards on the table, and the top card is ${hand.last()}")
}

class SmartPlayer : Player() {
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

class Game {
    private val dealer = Player()
    private val player = SmartPlayer()
    private val computer = Computer()
    private val table = Table()
    private var currentPlayer : Player = player

    fun start() {
        println("Indigo Card Game")
        currentPlayer = if (player.inputIsPlayerFirst()) player else computer

        dealer.hand.reset()
        dealer.hand.shuffle()
        repeat(4) { table.hand.add(dealer.play()) }
        print("Initial cards on the table: ")
        table.showHand()

        do {
            table.showTable()
            if (table.hand.size == DECK_SIZE) break // All the cards on the table
            if (player.hand.isEmpty() && computer.hand.isEmpty())
                repeat(6) {
                    player.hand.add(dealer.play())
                    computer.hand.add(dealer.play())
            }
            table.hand.add(currentPlayer.play())
            currentPlayer = if (currentPlayer == player) computer else player
        } while (true)
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
