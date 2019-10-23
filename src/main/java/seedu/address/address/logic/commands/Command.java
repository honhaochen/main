package seedu.address.address.logic.commands;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.address.model.AddressBookModel;

/**
 * Represents a command with hidden internal addressBookLogic and the ability to be executed.
 */
public abstract class Command extends seedu.address.logic.commands.Command<AddressBookModel> {

    /**
     * Executes the command and returns the result message.
     *
     * @param addressBookModel {@code AddressBookModel} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(AddressBookModel addressBookModel) throws CommandException;

}
