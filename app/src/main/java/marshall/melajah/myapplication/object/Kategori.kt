package marshall.melajah.myapplication.`object`

object Kategori {

    fun expenseCategory(): ArrayList<String> {
        val listExpense = ArrayList<String>()
        listExpense.add("Makanan dan Minuman")
        listExpense.add("Hiburan")
        listExpense.add("Hutang")
        listExpense.add("Investasi")
        listExpense.add("Top Up")
        listExpense.add("Pendidikan")
        listExpense.add("Sedekah")
        listExpense.add("Peliharaan")

        return listExpense
    }

    fun incomeCategory(): ArrayList<String> {
        val listIncome = ArrayList<String>()
        listIncome.add("Gaji/Upah")
        listIncome.add("Bonus")
        listIncome.add("Kerja Paruh Waktu")
        listIncome.add("Investasi")
        listIncome.add("Hadiah")

        return listIncome
    }
}