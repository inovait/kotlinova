package si.inova.kotlinova.ui

/**
 * Component (usually fragment) that provides its own title
 * @author Matej Drobnic
 */
interface TitledComponent {
    val title: String
}

interface SubtitledComponent : TitledComponent {
    val subtitle: String
}