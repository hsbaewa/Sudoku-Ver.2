package kr.co.hs.sudoku.model.settings

data class RegistrationEntity(
    var isFirstAppOpen: Boolean,
    var hasSeenSinglePlayGuide: Boolean,
    var hasSeenMultiPlayGuide: Boolean,
    var hasSeenChallengeGuide: Boolean,
    var hasSeenMultiPlayParticipateNotification: Boolean
)